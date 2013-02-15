package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

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
import com.lvl6.proto.InfoProto.UserBoosterPackProto;
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
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems = BoosterItemRetrieveUtils.getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      Map<Integer, Integer> boosterItemIdsToNumCollected = UserBoosterItemRetrieveUtils.getBoosterItemIdsToQuantityForUser(userId);

      //values to send to client
      List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
      Map<Integer, Integer> newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>(boosterItemIdsToNumCollected);
      List<Integer> userEquipIds = new ArrayList<Integer>();
      
      //keep track of amount user spent
      Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
          
      //check if user has enough money, has not purchased more than the limit for a booster pack,
      //and can still buy from the booster pack
      boolean legit = checkLegitPurchase(resBuilder, user, userId, now,
          aPack, boosterPackId, allBoosterItemIdsToBoosterItems, option, goldSilverChange);

      boolean successful = false;
      if (legit) {
        //check if user has bought up all the booster items in the booster pack
        int numBoosterItemsUserWants = determineNumBoosterItemsFromPurchaseOption(option);
        //boosterItemIdsToNumCollected will only be modified if the amount the user buys
        //is more than what is left in the deck
        itemsUserReceives = getAllBoosterItemsForUser(allBoosterItemIdsToBoosterItems,
            boosterItemIdsToNumCollected, numBoosterItemsUserWants);
        
        
        previousSilver  = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        successful = writeChangesToDB(resBuilder, user, boosterItemIdsToNumCollected,
            itemsUserReceives, goldSilverChange, userEquipIds, newBoosterItemIdsToNumCollected);
      }
      
      if (successful) {
        List<FullUserEquipProto> fullUserEquipProtos = 
            createFullUserEquipProtos(userEquipIds, userId, itemsUserReceives);
        UserBoosterPackProto aUserBoosterPackProto = 
            CreateInfoProtoUtils.createUserBoosterPackProto(boosterPackId, userId, newBoosterItemIdsToNumCollected);
        
        resBuilder.addAllUserEquips(fullUserEquipProtos);
        resBuilder.setUserBoosterPack(aUserBoosterPackProto);
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
      PurchaseOption option, Map<String, Integer> goldSilverChange) {
    
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

    List<Integer> cost = new ArrayList<Integer>();
    boolean validPurchaseOption = determineCostForPurchaseOption(aPack, option, cost);
    if(!validPurchaseOption) {
      resBuilder.setStatus(PurchaseBoosterPackStatus.OTHER_FAIL);
      log.error("Invalid purchase option: " + option + ", "
          + option.name() + ", " + option.getNumber());
      return false;
    }
    
    //check if user can afford to buy however many more user wants to buy
    if (!sufficientFunds(resBuilder, aUser, aPack, boosterPackId, cost.get(0), goldSilverChange)) {
      return false; //resBuilder status set in called function 
    }
    
    //check if user is within the limit of booster packs purchased within a day
    int numUserWantsToBuy = determineNumBoosterItemsFromPurchaseOption(option);
    if (!underDailyPurchaseLimit(resBuilder, userId, aPack, boosterPackId, nowTimestamp, numUserWantsToBuy)) {
      return false; //resBuilder status set in called function
    }
    
    resBuilder.setStatus(PurchaseBoosterPackStatus.SUCCESS);
    return true;
  }
  
  private boolean determineCostForPurchaseOption(BoosterPack aPack,
      PurchaseOption option, List<Integer> cost) {
    int costTemp = 0;
    if (PurchaseOption.ONE == option) {
      costTemp = aPack.getSalePriceOne();
      if (ControllerConstants.NOT_SET == costTemp) {
        costTemp = aPack.getRetailPriceOne();
      }
    } else if (PurchaseOption.TWO == option) {
      costTemp = aPack.getSalePriceTwo();
      if (ControllerConstants.NOT_SET == costTemp) {
        costTemp = aPack.getRetailPriceTwo();
      }
    } 
    
    if (ControllerConstants.NOT_SET == costTemp) {
      return false;
    } else {
      cost.add(costTemp);
      return true;
    }
  }
  
  private boolean sufficientFunds(Builder resBuilder, User aUser, 
      BoosterPack aPack, int boosterPackId, int cost,
      Map<String, Integer> goldSilverChange) {
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

  private int determineNumBoosterItemsFromPurchaseOption(PurchaseOption option) {
    if (PurchaseOption.ONE == option) {
      return ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_ONE_NUM_BOOSTER_ITEMS;
    } else {//if (PurchaseOption.TWO == option) {
      return ControllerConstants.BOOSTER_PACK__PURCHASE_OPTION_TWO_NUM_BOOSTER_ITEMS;
    } 
  }
  
  private boolean underDailyPurchaseLimit(Builder resBuilder, int userId, BoosterPack aPack, 
      int boosterPackId, Date now, int numUserWantsToBuy) {
    Timestamp startOfDayPstInUtc = MiscMethods.getPstDateAndHourFromUtcTime(now);
    int numPurchased = UserBoosterPackRetrieveUtils
        .getNumPacksPurchasedAfterDateForUserAndPackId(userId, boosterPackId, startOfDayPstInUtc);
    
    boolean limitSet = true;
    int dailyLimit = aPack.getDailyLimit();
    if (ControllerConstants.NOT_SET == dailyLimit) {
      limitSet = false;
    }
    int numUserWillHave = numPurchased + numUserWantsToBuy;
    if (limitSet && numUserWillHave > dailyLimit) {
      //user will have more than the limit
      int numMorePacksUserCanBuy = Math.max(0, dailyLimit - numPurchased);
      int minutesUntilLimitReset = determineTimeUntilReset(startOfDayPstInUtc, now);
      
      resBuilder.setStatus(PurchaseBoosterPackStatus.EXCEEDING_PURCHASE_LIMIT);
      resBuilder.setNumPacksToExceedLimit(numMorePacksUserCanBuy);
      resBuilder.setMinutesUntilLimitReset(minutesUntilLimitReset);
      log.error("user will have more booster packs than the limit: " + dailyLimit
          + " user has " + numPurchased + " and wants to buy " + numUserWantsToBuy + " more.");
      return false;
    }
    
    return true;
  }
  
  private int determineTimeUntilReset(Timestamp startOfDayPstInUtc, Date now) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(startOfDayPstInUtc);
    cal.add(Calendar.DATE, 1);
    long nextDayInMillis = cal.getTimeInMillis();
    long nowInMillis = now.getTime();
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    log.info("Date and time in PST: " + df.format(now));
    
    return (int) Math.ceil((nextDayInMillis - nowInMillis)/60000);
  }
  
  //Returns all the booster items the user purchased, if the user buys out deck
  //start over from a fresh deck (boosterItemIdsToNumCollected is changed to reflect none have been collected)
  private List<BoosterItem> getAllBoosterItemsForUser(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, int numBoosterItemsUserWants) {
    List<BoosterItem> returnValue = new ArrayList<BoosterItem>();
    
    //the possible items user can get
    List<Integer> boosterItemIdsUserCanGet = new ArrayList<Integer>();
    List<Integer> quantitiesInStock = new ArrayList<Integer>();
    
    //populate boosterItemIdsUserCanGet, and quantitiesInStock
    int sumQuantitiesInStock = determineBoosterItemsLeft(allBoosterItemIdsToBoosterItems, 
        boosterItemIdsToNumCollected, boosterItemIdsUserCanGet, quantitiesInStock);
    
    if (numBoosterItemsUserWants > sumQuantitiesInStock) {
      //give all the remaining booster items to the user, 
      for (int bItemId : boosterItemIdsUserCanGet) {
        returnValue.add(allBoosterItemIdsToBoosterItems.get(bItemId));
      }
      //decrement number user still needs to receive, and then reset deck
      numBoosterItemsUserWants -= sumQuantitiesInStock;
      
      //start from a clean slate as if it is the first time user is purchasing
      boosterItemIdsUserCanGet.clear();
      boosterItemIdsToNumCollected.clear();
      quantitiesInStock.clear();
      sumQuantitiesInStock = 0;
      
      for (int boosterItemId : allBoosterItemIdsToBoosterItems.keySet()) {
        BoosterItem boosterItemUserCanGet = allBoosterItemIdsToBoosterItems.get(boosterItemIdsUserCanGet);
        boosterItemIdsUserCanGet.add(boosterItemId);
        boosterItemIdsToNumCollected.put(boosterItemId, 0);
        int quantityInStock = boosterItemUserCanGet.getQuantity();
        quantitiesInStock.add(quantityInStock);
        sumQuantitiesInStock += quantityInStock;
      }
    }

    //set the booster item(s) the user will receieve
    List<BoosterItem> itemUserReceives = determineBoosterItemsUserReceives(boosterItemIdsUserCanGet, 
        quantitiesInStock, numBoosterItemsUserWants, sumQuantitiesInStock, allBoosterItemIdsToBoosterItems);
   
    returnValue.addAll(itemUserReceives);
    return returnValue;
  }
  
  //populates ids, quantitiesInStock; determines the remaining booster items the user can get
  private int determineBoosterItemsLeft(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock) {
    //max number randon number can go
    int sumQuantitiesInStock = 0;

    //determine how many BoosterItems are left that user can get
    for (int boosterItemId : allBoosterItemIdsToBoosterItems.keySet()) {
      BoosterItem potentialEquip = allBoosterItemIdsToBoosterItems.get(boosterItemId);
      int quantityLimit = potentialEquip.getQuantity();
      int quantityPurchasedPreviously = ControllerConstants.NOT_SET;

      if (boosterItemIdsToNumCollected.containsKey(boosterItemId)) {
        quantityPurchasedPreviously = boosterItemIdsToNumCollected.get(boosterItemId);
      }

      if(ControllerConstants.NOT_SET == quantityPurchasedPreviously) {
        //user has never bought this BoosterItem before
        boosterItemIdsUserCanGet.add(boosterItemId);
        quantitiesInStock.add(quantityLimit);
        sumQuantitiesInStock += quantityLimit;
      } else if (quantityPurchasedPreviously < quantityLimit) {
        //user bought before, but has not reached the limit
        int numLeftInStock = quantityLimit - quantityPurchasedPreviously;
        boosterItemIdsUserCanGet.add(boosterItemId);
        quantitiesInStock.add(numLeftInStock);
        sumQuantitiesInStock += numLeftInStock;
      }
    }
    
    return sumQuantitiesInStock;
  }
  
  private List<BoosterItem> determineBoosterItemsUserReceives(List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, int amountUserWantsToPurchase, int sumOfQuantitiesInStock,
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems) {
    List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
    Random rand = new Random();
    List<Integer> newBoosterItemIdsUserCanGet = new ArrayList<Integer>(boosterItemIdsUserCanGet);
    List<Integer> newQuantitiesInStock = new ArrayList<Integer>(quantitiesInStock);
    int newSumOfQuantities = sumOfQuantitiesInStock;
    
    //selects one of the ids at random without replacement
    for(int purchaseN = 0; purchaseN < amountUserWantsToPurchase; purchaseN++) {
      int sumSoFar = 0;
      int randomNum = rand.nextInt(newSumOfQuantities) + 1; //range [1, newSumOfQuantities]
      
      for(int i = 0; i < newBoosterItemIdsUserCanGet.size(); i++) {
        int bItemId = boosterItemIdsUserCanGet.get(i);
        int quantity = newQuantitiesInStock.get(i);
        
        sumSoFar += quantity;
        
        if(randomNum <= sumSoFar) {
          //we have a winner! current boosterItemId is what the user gets
          BoosterItem selectedBoosterItem = allBoosterItemIdsToBoosterItems.get(bItemId);
          itemsUserReceives.add(selectedBoosterItem);
          
          //preparation for next BoosterItem to be selected
          if (1 == quantity) {
            newBoosterItemIdsUserCanGet.remove(i);
            newQuantitiesInStock.remove(i);
          } else if (1 < quantity){
            //booster item id has more than one quantity
            int decrementedQuantity = newQuantitiesInStock.remove(i) - 1;
            newQuantitiesInStock.add(i, decrementedQuantity);
          } else {
            //ignore those with quantity of 0
            continue;
          }
          
          newSumOfQuantities -= 1;
          break;
        }
      }
    }
    
    return itemsUserReceives;
  }
  
  //sets values for newBoosterItemIdsToNumCollected
  private boolean writeChangesToDB(Builder resBuilder, User user, Map<Integer, Integer> boosterItemIdsToNumCollected,
      List<BoosterItem> itemsUserReceives, Map<String, Integer> goldSilverChange, List<Integer> uEquipIds,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected) {
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
    int currencyChange = goldSilverChange.get(key); //should be negative
    
    //update user's money
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
    } else {
      log.error("could not change user's money. Deleting equips bought: "
          + MiscMethods.shallowListToString(userEquipIds));
      DeleteUtils.get().deleteUserEquips(userEquipIds);
    }
    
    uEquipIds.addAll(userEquipIds);
    //update user_booster_items
    if (!updateUserBoosterItems(itemsUserReceives, boosterItemIdsToNumCollected,
        userId, newBoosterItemIdsToNumCollected)) {
      //failed to update user_booster_items
      log.error("failed to update user_booster_items for user: " + user
          + " attempting to give money back and delete equips bought: "
          + MiscMethods.shallowListToString(userEquipIds));
      if (MiscMethods.gold.equals(key)) {
        user.updateRelativeDiamondsNaive(-currencyChange);
      } else if (MiscMethods.silver.equals(key)){
        user.updateRelativeCoinsNaive(-currencyChange);
      }
      DeleteUtils.get().deleteUserEquips(userEquipIds);
      return false;
    }
    
    return true;
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
      Map<Integer, Integer> boosterItemIdsToNumCollected, int userId,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected) {
    Map<Integer, Integer> changedBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
    
    //for each booster item received record it in the map above, and record how many
    //booster items user has in aggregate
    for(BoosterItem received : itemsUserReceives) {
      int boosterItemId = received.getId();
      //default quantity user gets if user has no quantity of specific boosterItem
      int newQuantity = 1; 
      if(boosterItemIdsToNumCollected.containsKey(boosterItemId)) {
        newQuantity = boosterItemIdsToNumCollected.get(boosterItemId) + 1;
      }
      changedBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
      newBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
    }
    return UpdateUtils.get().updateUserBoosterItemsForOneUser(userId, changedBoosterItemIdsToNumCollected);
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
