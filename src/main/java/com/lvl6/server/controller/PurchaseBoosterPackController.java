package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.hazelcast.core.IList;
import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseBoosterPackRequestEvent;
import com.lvl6.events.response.PurchaseBoosterPackResponseEvent;
import com.lvl6.events.response.ReceivedRareBoosterPurchaseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseBoosterPackRequestProto;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto.PurchaseBoosterPackStatus;
import com.lvl6.proto.EventProto.ReceivedRareBoosterPurchaseResponseProto;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PurchaseOption;
import com.lvl6.proto.InfoProto.RareBoosterPurchaseProto;
import com.lvl6.proto.InfoProto.UserBoosterPackProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.UserBoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.server.EventWriter;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;

@Component @DependsOn("gameServer") public class PurchaseBoosterPackController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public static int BOOSTER_PURCHASES_MAX_SIZE = 50;

  @Resource
  protected EventWriter eventWriter;

  public EventWriter getEventWriter() {
    return eventWriter;
  }

  public void setEventWriter(EventWriter eventWriter) {
    this.eventWriter = eventWriter;
  }

  
	@Resource(name = "goodEquipsRecievedFromBoosterPacks")
	protected IList<RareBoosterPurchaseProto> goodEquipsRecievedFromBoosterPacks;
	public IList<RareBoosterPurchaseProto> getGoodEquipsRecievedFromBoosterPacks() {
		return goodEquipsRecievedFromBoosterPacks;
	}

	public void setGoodEquipsRecievedFromBoosterPacks(
			IList<RareBoosterPurchaseProto> goodEquipsRecievedFromBoosterPacks) {
		this.goodEquipsRecievedFromBoosterPacks = goodEquipsRecievedFromBoosterPacks;
	}
  
  
  
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
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems = BoosterItemRetrieveUtils.getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      Map<Integer, Integer> boosterItemIdsToNumCollected = UserBoosterItemRetrieveUtils.getBoosterItemIdsToQuantityForUser(userId);

      //values to send to client
      List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
      Map<Integer, Integer> newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
      List<Integer> userEquipIds = new ArrayList<Integer>();
      
      List<Boolean> collectedBeforeReset = new ArrayList<Boolean>();
      
      //keep track of amount user spent
      Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
          
      //check if user has enough money, has not purchased more than the limit for a booster pack,
      //and can still buy from the booster pack
      boolean legit = checkLegitPurchase(resBuilder, user, userId, now,
          aPack, boosterPackId, boosterItemIdsToBoosterItems, option, goldSilverChange);

      boolean successful = false;
      if (legit) {
        //check if user has bought up all the booster items in the booster pack
        int numBoosterItemsUserWants = determineNumBoosterItemsFromPurchaseOption(option);
        if (aPack.isStarterPack()) {
          //starter pack means user gets one weapon, one armor, and one equip
          numBoosterItemsUserWants = 3;
        }
        
        //boosterItemIdsToNumCollected will only be modified if the amount the user buys
        //is more than what is left in the deck, need to reflect this in newBoosterItemIdsToNumCollected
        //and need to track how many items user received to buy out pack
        boolean resetOccurred = MiscMethods.getAllBoosterItemsForUser(boosterItemIdsToBoosterItems, boosterItemIdsToNumCollected,
            numBoosterItemsUserWants, user, aPack, itemsUserReceives, collectedBeforeReset);
        newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>(boosterItemIdsToNumCollected);
        
        previousSilver  = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        successful = writeChangesToDB(resBuilder, user, boosterItemIdsToNumCollected,
            newBoosterItemIdsToNumCollected, itemsUserReceives, collectedBeforeReset, 
            goldSilverChange, userEquipIds, resetOccurred);
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
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        int numBought = itemsUserReceives.size();
        MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, boosterPackId, numBought, 
            nowTimestamp, itemsUserReceives);
        writeToUserCurrencyHistory(user, boosterPackId, nowTimestamp,
            goldSilverChange, previousSilver, previousGold);
        
        sendBoosterPurchaseMessage(user, aPack, itemsUserReceives);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseBoosterPackController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName()); 
    }
  }

  private void sendBoosterPurchaseMessage(User user, BoosterPack aPack, List<BoosterItem> itemsUserReceives) {
    Map<Integer, Equipment> equipMap = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    Date d = new Date();
    for (BoosterItem bi : itemsUserReceives) {
      Equipment eq = equipMap.get(bi.getEquipId());
//      if (eq.getRarity().compareTo(Rarity.SUPERRARE) >= 0) {
        RareBoosterPurchaseProto r = CreateInfoProtoUtils.createRareBoosterPurchaseProto(aPack, user, eq, d);
        
        goodEquipsRecievedFromBoosterPacks.add(0, r);
        //remove older messages
        try {
          while(goodEquipsRecievedFromBoosterPacks.size() > BOOSTER_PURCHASES_MAX_SIZE) {
            goodEquipsRecievedFromBoosterPacks.remove(BOOSTER_PURCHASES_MAX_SIZE);
          }
        } catch(Exception e) {
          log.error("Error adding rare booster purchase.", e);
        }
        
        ReceivedRareBoosterPurchaseResponseProto p = ReceivedRareBoosterPurchaseResponseProto.newBuilder().setRareBoosterPurchase(r).build();
        ReceivedRareBoosterPurchaseResponseEvent e = new ReceivedRareBoosterPurchaseResponseEvent(user.getId());
        e.setReceivedRareBoosterPurchaseResponseProto(p);
        eventWriter.processGlobalChatResponseEvent(e);
//      }
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
    cal.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    cal.setTime(startOfDayPstInUtc);
    cal.add(Calendar.DATE, 1);
    long nextDayInMillisGmt = cal.getTimeInMillis();

    return (int) Math.ceil((nextDayInMillisGmt - now.getTime())/60000);
  }
  
  /*moved all to misc methods
  //Returns all the booster items the user purchased and whether or not the use reset the chesst.
  //If the user buys out deck start over from a fresh deck 
  //(boosterItemIdsToNumCollected is changed to reflect none have been collected).
  //Also, keep track of which items were purchased before and/or after the reset (via collectedBeforeReset)
  private boolean getAllBoosterItemsForUser(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, int numBoosterItemsUserWants, User aUser, 
      BoosterPack aPack, List<BoosterItem> returnValue, List<Boolean> collectedBeforeReset) {
    boolean resetOccurred = false;
    int boosterPackId = aPack.getId();
    
    //the possible items user can get
    List<Integer> boosterItemIdsUserCanGet = new ArrayList<Integer>();
    List<Integer> quantitiesInStock = new ArrayList<Integer>();
    
    //populate boosterItemIdsUserCanGet, and quantitiesInStock
    int sumQuantitiesInStock = determineBoosterItemsLeft(allBoosterItemIdsToBoosterItems, 
        boosterItemIdsToNumCollected, boosterItemIdsUserCanGet, quantitiesInStock, aUser, boosterPackId);
    
    //just in case user is allowed to purchase a lot more than what is available in a chest
    //should take care of the case where user buys out the exact amount remaining in the chest
    while (numBoosterItemsUserWants >= sumQuantitiesInStock) {
      resetOccurred = true;
      //give all the remaining booster items to the user, 
      for (int i = 0; i < boosterItemIdsUserCanGet.size(); i++) {
        int bItemId = boosterItemIdsUserCanGet.get(i);
        BoosterItem bi = allBoosterItemIdsToBoosterItems.get(bItemId);
        int quantityInStock = quantitiesInStock.get(i);
        for (int quant = 0; quant < quantityInStock; quant++) {
          returnValue.add(bi);
          collectedBeforeReset.add(true);
        }
      }
      //decrement number user still needs to receive, and then reset deck
      numBoosterItemsUserWants -= sumQuantitiesInStock;
      
      //start from a clean slate as if it is the first time user is purchasing
      boosterItemIdsUserCanGet.clear();
      boosterItemIdsToNumCollected.clear();
      quantitiesInStock.clear();
      sumQuantitiesInStock = 0;
      for (int boosterItemId : allBoosterItemIdsToBoosterItems.keySet()) {
        BoosterItem boosterItemUserCanGet = allBoosterItemIdsToBoosterItems.get(boosterItemId);
        boosterItemIdsUserCanGet.add(boosterItemId);
        boosterItemIdsToNumCollected.put(boosterItemId, 0);
        int quantityInStock = boosterItemUserCanGet.getQuantity();
        quantitiesInStock.add(quantityInStock);
        sumQuantitiesInStock += quantityInStock;
      }
    }

    //set the booster item(s) the user will receieve  
    List<BoosterItem> itemUserReceives = new ArrayList<BoosterItem>();
    if (aPack.isStarterPack()) {
      itemUserReceives = determineStarterBoosterItemsUserReceives(boosterItemIdsUserCanGet,
          quantitiesInStock, numBoosterItemsUserWants, sumQuantitiesInStock, allBoosterItemIdsToBoosterItems);
    } else {
      itemUserReceives = determineBoosterItemsUserReceives(boosterItemIdsUserCanGet, 
          quantitiesInStock, numBoosterItemsUserWants, sumQuantitiesInStock, allBoosterItemIdsToBoosterItems);
    }
    returnValue.addAll(itemUserReceives);
    collectedBeforeReset.addAll(Collections.nCopies(itemUserReceives.size(), false));
    return resetOccurred;
  }
  
  //populates ids, quantitiesInStock; determines the remaining booster items the user can get
  private int determineBoosterItemsLeft(Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems, 
      Map<Integer, Integer> boosterItemIdsToNumCollected, List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, User aUser, int boosterPackId) {
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
      } else if (quantityPurchasedPreviously == quantityLimit) {
        continue;
      } else {//will this ever be reached?
        log.error("somehow user has bought more than the allowed limit for a booster item for a booster pack. "
            + "quantityLimit: " + quantityLimit + ", quantityPurchasedPreviously: " + quantityPurchasedPreviously
            + ", userId: " + aUser.getId() + ", boosterItem: " + potentialEquip + ", boosterPackId: " + boosterPackId);
      }
    }
    
    return sumQuantitiesInStock;
  }
  
  //no arguments are modified
  private List<BoosterItem> determineStarterBoosterItemsUserReceives(List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, int amountUserWantsToPurchase, int sumOfQuantitiesInStock,
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems) {
    //return value
    List<BoosterItem> returnValue = new ArrayList<BoosterItem>();
    if (0 == amountUserWantsToPurchase) {
      return returnValue;
    } else if (3 != amountUserWantsToPurchase) {
      log.error("unexpected error: buying " + amountUserWantsToPurchase + " more equips instead of 3.");
      return returnValue; 
    } else if (0 != (sumOfQuantitiesInStock % 3)) {
      log.error("unexpected error: num remaining equips, " + sumOfQuantitiesInStock
          + ", for this chest is not a multiple of 3");
      return returnValue;
    }
    
    Map<Integer, Equipment> allEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    Set<EquipType> receivedEquipTypes = new HashSet<EquipType>();
    
    //loop through equips user can get; select one weapon, one armor, one amulet
    for (int boosterItemId : boosterItemIdsUserCanGet) {
      BoosterItem bi = allBoosterItemIdsToBoosterItems.get(boosterItemId);
      int equipId = bi.getEquipId();
      Equipment equip = allEquips.get(equipId);
      EquipType eType = equip.getType();
      
      if (receivedEquipTypes.contains(eType)) {
        //user already got this equip type
        continue;
      } else {
        //record user got a new equip type
        returnValue.add(bi);
        receivedEquipTypes.add(eType);
      }
    }
    
    if (3 != returnValue.size()) {
      log.error("unexpected error: user did not receive one type of each equip."
      		+ " User would have received (but now will not receive): " + MiscMethods.shallowListToString(returnValue) 
      		+ ". Chest either intialized improperly or code assigns equips incorrectly.");
      return new ArrayList<BoosterItem>();
    }
    return returnValue;
  }
  
  //no arguments are modified
  private List<BoosterItem> determineBoosterItemsUserReceives(List<Integer> boosterItemIdsUserCanGet, 
      List<Integer> quantitiesInStock, int amountUserWantsToPurchase, int sumOfQuantitiesInStock,
      Map<Integer, BoosterItem> allBoosterItemIdsToBoosterItems) {
    //return value
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
        int bItemId = newBoosterItemIdsUserCanGet.get(i);
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
  } */
  
  //sets values for newBoosterItemIdsToNumCollected
  private boolean writeChangesToDB(Builder resBuilder, User user, Map<Integer, Integer> boosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, List<BoosterItem> itemsUserReceives, 
      List<Boolean> collectedBeforeReset, Map<String, Integer> goldSilverChange, List<Integer> uEquipIds, boolean resetOccurred) {
    //insert into user_equips, update user, update user_booster_items
    int userId = user.getId();
    List<Integer> userEquipIds = MiscMethods.insertNewUserEquips(userId, itemsUserReceives);
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
    if (!MiscMethods.updateUserBoosterItems(itemsUserReceives, collectedBeforeReset, 
        boosterItemIdsToNumCollected, newBoosterItemIdsToNumCollected, userId, resetOccurred)) {
      //failed to update user_booster_items
      log.error("failed to update user_booster_items for userId: " + userId
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
  /* Moved to misc methods
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
      List<Boolean> collectedBeforeReset, Map<Integer, Integer> boosterItemIdsToNumCollected, 
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, int userId, boolean resetOccurred) {
    
    Map<Integer, Integer> changedBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
    int numCollectedBeforeReset = 0;

    //for each booster item received record it in the map above, and record how many
    //booster items user has in aggregate
    for (int i = 0; i < itemsUserReceives.size(); i++) {
      boolean beforeReset = collectedBeforeReset.get(i);
      if (!beforeReset) {
        BoosterItem received = itemsUserReceives.get(i);
        int boosterItemId = received.getId();
        
        //default quantity user gets if user has no quantity of specific boosterItem
        int newQuantity = 1; 
        if(newBoosterItemIdsToNumCollected.containsKey(boosterItemId)) {
          newQuantity = newBoosterItemIdsToNumCollected.get(boosterItemId) + 1;
        }
        changedBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
        newBoosterItemIdsToNumCollected.put(boosterItemId, newQuantity);
      } else {
        numCollectedBeforeReset++;
      }
    }
    
    //loop through newBoosterItemIdsToNumCollected and make sure the quantities
    //collected is itemsUserReceives.size() amount more than boosterItemIdsToNumCollected
    int changeInCollectedQuantity = 0;
    for (int id : changedBoosterItemIdsToNumCollected.keySet()) {
      int newAmount = newBoosterItemIdsToNumCollected.get(id);
      int oldAmount = 0;
      if (boosterItemIdsToNumCollected.containsKey(id)) {
        oldAmount = boosterItemIdsToNumCollected.get(id);
      }
      changeInCollectedQuantity += newAmount - oldAmount;
    }
    //for when user buys out a pack and then some
    changeInCollectedQuantity += numCollectedBeforeReset;
    if (itemsUserReceives.size() != changeInCollectedQuantity) {
      log.error("quantities of booster items do not match how many items user receives. "
          + "amount user receives that is recorded (user_booster_items table): " + changeInCollectedQuantity
          + ", amount user receives (unrecorded): " + itemsUserReceives.size());
      return false;
    }

    recordBoosterItemsThatReset(changedBoosterItemIdsToNumCollected, newBoosterItemIdsToNumCollected, resetOccurred);
    
    return UpdateUtils.get().updateUserBoosterItemsForOneUser(userId, changedBoosterItemIdsToNumCollected);
  }
  
  //if the user has bought out the whole deck, then for the booster items
  //the user did not get, record in the db that the user has 0 of them collected
  private void recordBoosterItemsThatReset(Map<Integer, Integer> changedBoosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, boolean refilled) {
    if (refilled) {
      for (int boosterItemId : newBoosterItemIdsToNumCollected.keySet()) {
        if (!changedBoosterItemIdsToNumCollected.containsKey(boosterItemId)) {
          int value = newBoosterItemIdsToNumCollected.get(boosterItemId);
          changedBoosterItemIdsToNumCollected.put(boosterItemId, value);
        }
      }
    }
  }*/
  
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
