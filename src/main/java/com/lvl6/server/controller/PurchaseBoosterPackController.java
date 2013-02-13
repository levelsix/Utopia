package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseBoosterPackRequestEvent;
import com.lvl6.events.response.PurchaseBoosterPackResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseBoosterPackRequestProto;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto.PurchaseBoosterPackStatus;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PurchaseOption;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.UserBoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class PurchaseBoosterPackController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PurchaseBoosterPackController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseBoosterPackRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_BOOSTER_PACK_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseBoosterPackRequestProto reqProto = ((PurchaseBoosterPackRequestEvent)event).getPurchaseBoosterPackRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int boosterPackId = reqProto.getBoosterPackId();
    PurchaseOption option = reqProto.getPurchaseOption();
    Date now = new Date(reqProto.getClientTime());

    PurchaseBoosterPackResponseProto.Builder resBuilder = PurchaseBoosterPackResponseProto.newBuilder();
    resBuilder.setSender(senderProto);


    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      int userId = senderProto.getUserId();
      User user = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      int previousSilver = 0;
      int previousGold = 0;
      BoosterPack aPack = BoosterPackRetrieveUtils.getBoosterPackForBoosterPackId(boosterPackId);
      Map<Integer, BoosterItem> idsToItems = BoosterItemRetrieveUtils.getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      Map<Integer, Integer> userItemIdsToQuantities = UserBoosterItemRetrieveUtils.getBoosterItemIdsToQuantityForUser(userId);
      List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
      //keep track of amount user spent
      Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
          
      //check if user has enough money, has not purchased more than the limit for a booster pack,
      //and can still buy from the booster pack
      boolean legit = checkLegitPurchase(resBuilder, user, userId, now,
          aPack, boosterPackId, idsToItems, userItemIdsToQuantities,
          option, goldSilverChange, itemsUserReceives);

      boolean successful = false;
      List<Integer> userEquipIds = new ArrayList<Integer>();
      List<FullUserEquipProto> protos;
      if (legit) {
        previousSilver  = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        successful = writeChangesToDB(resBuilder, user, userItemIdsToQuantities,
            itemsUserReceives, goldSilverChange, userEquipIds);
      }
      
      if (successful) {
        protos = createFullUserEquipProtos(userEquipIds, userId, itemsUserReceives);
        resBuilder.addAllUserEquips(protos);
      }
      
      PurchaseBoosterPackResponseProto resProto = resBuilder.build();
      PurchaseBoosterPackResponseEvent resEvent = new PurchaseBoosterPackResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseBoosterPackResponseProto(resProto);
      server.writeEvent(resEvent);
      
      if (successful) {
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        int numBought = itemsUserReceives.size();
        writeToUserBoosterPackHistory(userId, boosterPackId, numBought, nowTimestamp);
        writeToUserCurrencyHistory(user, boosterPackId, nowTimestamp,
            goldSilverChange, previousSilver, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseBoosterPackController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName()); 
    }
  }
  
  private boolean checkLegitPurchase(Builder resBuilder, User aUser, int userId, 
      Date now, BoosterPack aPack, int boosterPackId, Map<Integer, BoosterItem> items,
      Map<Integer, Integer> userItemIdsToQuantities, PurchaseOption option, 
      Map<String, Integer> goldSilverChange, List<BoosterItem> itemsUserReceives) {
    
    if (null == aUser || null == aPack || null == items || items.isEmpty()) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      log.error("no user for id: " + userId + ", or no BoosterPack for id: "
          + boosterPackId + ", or no booster items for BoosterPack id");
      return false;
    }
    Timestamp nowTimestamp = new Timestamp(now.getTime());
    if (!MiscMethods.checkClientTimeAroundApproximateNow(nowTimestamp)) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + nowTimestamp
          + ", servertime~=" + new Date());
      return false;
    }
    
    List<Integer> numBoosterItemsUserWants = new ArrayList<Integer>();
    //check if user can afford to buy however many more user wants to buy
    if (!sufficientFunds(resBuilder, aUser, aPack, boosterPackId, option, goldSilverChange, numBoosterItemsUserWants)) {
      return false; //resBuilder status set in called function 
    }
    
    //check if user is within the limit of booster packs purchased within a day
    if (!underPurchaseLimit(resBuilder, userId, boosterPackId, nowTimestamp, option)) {
      return false; //resBuilder status set in called function
    }
    
    //check if user has bought up all the booster items in the booster pack
    if (didBuyOutBoosterPack(resBuilder, userId, boosterPackId, items, userItemIdsToQuantities,
        option, itemsUserReceives, numBoosterItemsUserWants)) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.BOOSTER_PACK_SOLD_OUT);
      return false;
    }
    
    resBuilder.setStatus(PurchaseBoosterPackStatus.SUCCESS);
    return true;
  }
  
  private boolean sufficientFunds(Builder resBuilder, User aUser, 
      BoosterPack aPack, int boosterPackId, PurchaseOption option,
      Map<String, Integer> goldSilverChange, List<Integer> numBoosterItemsUserWants) {
    String key = "";
    boolean costsSilver = aPack.isCostsCoins();
    int userFunds = 0;
    PurchaseBoosterPackStatus notEnough;
    if(costsSilver) {
      userFunds = aUser.getCoins();
      notEnough = PurchaseBoosterPackStatus.NOT_ENOUGH_SILVER;
      key = MiscMethods.silver;
    } else {
      userFunds = aUser.getDiamonds();
      notEnough = PurchaseBoosterPackStatus.NOT_ENOUGH_GOLD;
      key = MiscMethods.gold;
    }
    int cost = determineCost(aPack, option, numBoosterItemsUserWants);
    if (ControllerConstants.NOT_SET == cost) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      log.warn("booster pack with id=" + boosterPackId + " has no price.");
      return false;
    }
    if (userFunds < cost) {
      resBuilder.setStatus(notEnough);
      log.error("user does not have enough gold or silver. Has " + userFunds
          + " but needs " + cost);
      return false;
    }
    
    goldSilverChange.put(key, -cost);
    return true;
  }

  private int determineCost(BoosterPack aPack, PurchaseOption option,
      List<Integer> numBoosterItemsUserWants) {
    int cost = 0;
    if(PurchaseOption.ONE == option) { //one item
      numBoosterItemsUserWants.add(ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_ONE);
      cost = aPack.getRetailPriceOne();
      if (ControllerConstants.NOT_SET == cost) {
        cost = aPack.getSalePriceOne();
      }
    } else {// ten items
      numBoosterItemsUserWants.add(ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_TWO);
      cost = aPack.getRetailPriceTwo();
      if (ControllerConstants.NOT_SET == cost) {
        cost = aPack.getSalePriceTwo();
      }
    }
    return cost;
  }
  
  private boolean underPurchaseLimit(Builder resBuilder, int userId, int boosterPackId,
      Date now, PurchaseOption option) {
    Timestamp startOfDayPstInUtc = MiscMethods.getPstDateAndHourFromUtcTime(now);
    int numPurchased = UserBoosterPackRetrieveUtils
        .getNumPacksPurchasedAfterDateForUserAndPackId(userId, boosterPackId, startOfDayPstInUtc);
    int numToBuy = 0;
    if(PurchaseOption.ONE == option) {
      numToBuy += ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_ONE;
    } else if (PurchaseOption.TWO == option){
      numToBuy += ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_TWO;
    } else {
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      log.error("invalid purchase option: " + option + ", " + option.getNumber());
      return false;
    }
    
    int total = numPurchased + numToBuy;
    if (ControllerConstants.BOOSTER_PACK__PURCHASE_LIMIT < total) {
      //user will have more than the limit
      resBuilder.setStatus(PurchaseBoosterPackStatus.EXCEEDING_PURCHASE_LIMIT);
      log.error("user will have more booster packs than the limit. user has "
          + numPurchased + " and wants to buy " + numToBuy + " more.");
      return false;
    }
    
    return true;
  }
  
  //successful purchase, this function returns false
  private boolean didBuyOutBoosterPack(Builder resBuilder, int userId, int boosterPackId, 
      Map<Integer, BoosterItem> boosterItems, Map<Integer, Integer> userItemIdsToQuantities, 
      PurchaseOption option, List<BoosterItem> itemsUserReceives,
      List<Integer> numBoosterItemsUserWants) {
    List<Integer> boosterItemIds = new ArrayList<Integer>();
    List<Integer> quantitiesInStock = new ArrayList<Integer>();
    
    //max number randon number can go
    int totalAvailableItems = determineBoosterItemsLeft(boosterItems, userItemIdsToQuantities, 
        boosterItemIds, quantitiesInStock);
    
    if (0 == totalAvailableItems) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.BOOSTER_PACK_SOLD_OUT);
      log.error("No more booster items in booster pack with id: " + boosterPackId);
      return true;
    } 
    
    if (numBoosterItemsUserWants.isEmpty()){
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      log.error("wrong type of PurchaseOption sent: " + option + ", " + option.getNumber());
      return true;
    }
    int amountUserWantsToPurchase = numBoosterItemsUserWants.get(0);
    
    if(amountUserWantsToPurchase > totalAvailableItems) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.EXCEEDING_PURCHASE_LIMIT);
      log.error("user " + userId + " wants to buy " + amountUserWantsToPurchase
          + " and there are only " + totalAvailableItems + " items left");
      return true;
    }
    
    //set the booster item(s) the user will receieve
    determineBoosterItemsUserReceives(boosterItemIds, quantitiesInStock,
        amountUserWantsToPurchase, totalAvailableItems, boosterItems, itemsUserReceives);
    
    return false;
  }
  
  private int determineBoosterItemsLeft(Map<Integer, BoosterItem> items, 
      Map<Integer, Integer> userItemIdsToQuantities, List<Integer> ids,
      List<Integer> quantitiesInStock) {
    //max number randon number can go
    int totalAvailableItems = 0;

    //determine how many BoosterItems are left that user can get
    for (int boosterItemId : items.keySet()) {
      BoosterItem potentialEquip = items.get(boosterItemId);
      int quantityLimit = potentialEquip.getQuantity();
      int quantityPurchased = ControllerConstants.NOT_SET;

      if (userItemIdsToQuantities.containsKey(boosterItemId)) {
        quantityPurchased = userItemIdsToQuantities.get(boosterItemId);
      }

      //if user bought item before and is under the limit
      if(ControllerConstants.NOT_SET == quantityPurchased) {
        //user has never bought this BoosterItem before
        ids.add(boosterItemId);
        quantitiesInStock.add(quantityLimit);
        totalAvailableItems += quantityLimit;
      } else if (quantityPurchased < quantityLimit) {
        //user bought before, but has not reached the limit
        int numLeftToGet = quantityLimit - quantityPurchased;
        ids.add(boosterItemId);
        quantitiesInStock.add(numLeftToGet);
        totalAvailableItems += numLeftToGet;
      }
    }
    
    return totalAvailableItems;
  }
  
  private void determineBoosterItemsUserReceives(List<Integer> boosterItemIds, 
      List<Integer> quantities, int amountUserWantsToPurchase, int sumOfQuantities,
      Map<Integer, BoosterItem> boosterItems, List<BoosterItem> itemsUserReceives) {
    Random rand = new Random();
    List<Integer> newBoosterItemIds = new ArrayList<Integer>(boosterItemIds);
    List<Integer> newQuantities = new ArrayList<Integer>(quantities);
    int newSumOfQuantities = sumOfQuantities;
    
    //selects one of the ids at random without replacement
    for(int purchaseN = 0; purchaseN < amountUserWantsToPurchase; purchaseN++) {
      int sumSoFar = 0;
      int randomNum = rand.nextInt(newSumOfQuantities) + 1; //range [1, newSumOfQuantities]
      
      for(int i = 0; i < newBoosterItemIds.size(); i++) {
        int bItemId = boosterItemIds.get(i);
        int quantity = newQuantities.get(i);
        
        sumSoFar += quantity;
        
        if(randomNum <= sumSoFar) {
          //we have a winner! current boosterItemId is what the user gets
          BoosterItem selectedBoosterItem = boosterItems.get(bItemId);
          itemsUserReceives.add(selectedBoosterItem);
          
          //preparation for next BoosterItem to be selected
          if (1 == quantity) {
            newBoosterItemIds.remove(i);
            newQuantities.remove(i);
          } else if (1 < quantity){
            //booster item id has more than one quantity
            int decrementedQuantity = newQuantities.remove(i) - 1;
            newQuantities.add(i, decrementedQuantity);
          } else {
            //quantity should not be 0
            log.error("quantity for booster item with id " + bItemId
                + " has a quantity of 0");
            continue;
          }
          
          newSumOfQuantities -= 1;
          break;
        }
      }
    }
  }
  
  private boolean writeChangesToDB(Builder resBuilder, User user, Map<Integer, Integer> userItemIdsToQuantities,
      List<BoosterItem> itemsUserReceives, Map<String, Integer> goldSilverChange, List<Integer> uEquipIds) {
    //insert into user_equips, update user, update user_booster_items
    int userId = user.getId();
    List<Integer> userEquipIds = insertNewUserEquips(userId, itemsUserReceives);
    if (null == userEquipIds || userEquipIds.isEmpty() 
        || userEquipIds.size() != itemsUserReceives.size()) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      return false;
    }
    
    //user received the correct number of equips
    String key = (String) (goldSilverChange.keySet().toArray())[0];
    int currencyChange = goldSilverChange.get(key);
    
    if (MiscMethods.gold.equals(key)) {
      if (!user.updateRelativeDiamondsNaive(currencyChange)) {
        log.error("could not change user's money. Deleting equips bought: "
            + MiscMethods.shallowListToString(userEquipIds));
        DeleteUtils.get().deleteUserEquips(userEquipIds);
        return false;
      }
    } else if (MiscMethods.silver.equals(key)){
      if (!user.updateRelativeCoinsNaive(currencyChange)) {
        log.error("could not change user's money. Deleting equips bought: "
            + MiscMethods.shallowListToString(userEquipIds));
        DeleteUtils.get().deleteUserEquips(userEquipIds);
        return false;
      }
    }
    
    uEquipIds.addAll(userEquipIds);
    return updateUserBoosterItems(itemsUserReceives, userItemIdsToQuantities, userId);
    
  }
  
  private List<Integer> insertNewUserEquips(int userId, List<BoosterItem> itemsUserReceives) {
    int amount = itemsUserReceives.size();
    int forgeLevel = ControllerConstants.DEFAULT_USER_EQUIP_LEVEL;
    int enhancementLevel = ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT;
    List<Integer> equipIds = new ArrayList<Integer>();
    List<Integer> levels = new ArrayList<Integer>(Collections.nCopies(amount, forgeLevel));
    List<Integer> enhancement = new ArrayList<Integer>(Collections.nCopies(amount, enhancementLevel));
    
    for(BoosterItem bi : itemsUserReceives) {
      int equipId = bi.getEquipId();
      equipIds.add(equipId);
    }
    
    return InsertUtils.get().insertUserEquips(userId, equipIds, levels, enhancement);
  }
  
  private boolean updateUserBoosterItems(List<BoosterItem> itemsUserReceives,
      Map<Integer, Integer> userItemIdsToQuantities, int userId) {
    //log.info("booster items: " + MiscMethods.shallowListToString(itemsUserReceives));
    //log.info("boosterItemIds to quantities: " + MiscMethods.shallowMapToString(userItemIdsToQuantities));
    for(BoosterItem received : itemsUserReceives) {
      int boosterItemId = received.getId();
      //default quantity user gets if user has no quantity of specific boosterItem
      int newQuantity = 1; 
      if(userItemIdsToQuantities.containsKey(boosterItemId)) {
        newQuantity = userItemIdsToQuantities.get(boosterItemId) + 1;
      }
      userItemIdsToQuantities.put(boosterItemId, newQuantity);
    }
    return UpdateUtils.get().updateUserBoosterItemsForOneUser(userId, userItemIdsToQuantities);
  }
  
  private List<FullUserEquipProto> createFullUserEquipProtos(List<Integer> userEquipIds, 
      int userId, List<BoosterItem> boosterItems) {
    List<FullUserEquipProto> protos = new ArrayList<FullUserEquipProto>();
    
    for(int i = 0; i < boosterItems.size(); i++) {
      int ueId = userEquipIds.get(i);
      BoosterItem bi = boosterItems.get(i);
      int equipId = bi.getEquipId();
      int level = ControllerConstants.DEFAULT_USER_EQUIP_LEVEL;
      int enhancement = ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT; 
      UserEquip ue = new UserEquip(ueId, userId, equipId, level, enhancement);
      
      FullUserEquipProto fuep = CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue);
      protos.add(fuep);
    }
    
    return protos;
  }
  
  private void writeToUserBoosterPackHistory(int userId, int packId,
      int numBought, Timestamp nowTimestamp) {
    InsertUtils.get().insertIntoUserBoosterPackHistory(userId,
        packId, numBought, nowTimestamp);
  }
  
  private void writeToUserCurrencyHistory(User aUser, int packId, Timestamp date, Map<String, Integer> money,
      int previousSilver, int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__PURHCASED_BOOSTER_PACK + packId;
    
    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
  
}
