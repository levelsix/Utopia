package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.lvl6.utils.RetrieveUtils;

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
      //keep track of amount user spent
      Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
          
      boolean legit = checkLegitPurchase(resBuilder, user, userId, now,
          aPack, boosterPackId, idsToItems, userItemIdsToQuantities,
          option, goldSilverChange);

      boolean successful = false;
      List<FullUserEquipProto> protos = new ArrayList<FullUserEquipProto>();
      if (legit) {
        previousSilver  = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        //successful = writeChangesToDB();
      }
      
      if (successful) {
        resBuilder.addAllUserEquips(protos);
      }
      
      PurchaseBoosterPackResponseProto resProto = resBuilder.build();
      PurchaseBoosterPackResponseEvent resEvent = new PurchaseBoosterPackResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseBoosterPackResponseProto(resProto);
      server.writeEvent(resEvent);
      
      if (successful) {
        Timestamp nowTimestamp = new Timestamp(now.getTime());
        //writeToUserBoosterPackHistory();
        //writeToUserCurrencyHistory(user, nowTimestamp, goldSilverChange, previousSilver, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in ArmoryController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName()); 
    }
  }
  
  private boolean checkLegitPurchase(Builder resBuilder, User aUser, int userId, 
      Date now, BoosterPack aPack, int boosterPackId, Map<Integer, BoosterItem> items,
      Map<Integer, Integer> userItemIdsToQuantities, PurchaseOption option, 
      Map<String, Integer> goldSilverChange) {
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
    //check if user can afford to buy however many more user wants to buy
    if (!sufficientFunds(resBuilder, aUser, aPack, boosterPackId, option, goldSilverChange)) {
      return false;
    }
    
    //check if user is within the limit of booster packs purchased within a day
    if (!underPurchaseLimit(resBuilder, userId, boosterPackId, nowTimestamp, option)) {
      return false;
    }
    
    //check if 
    return true;
  }
  
  private boolean sufficientFunds(Builder resBuilder, User aUser, 
      BoosterPack aPack, int boosterPackId, PurchaseOption option,
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
    int cost = determineCost(aPack, option);
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

  private int determineCost(BoosterPack aPack, PurchaseOption option) {
    int cost = 0;
    if(PurchaseOption.ONE == option) { //one item
      cost = aPack.getRetailPriceOne();
      if (ControllerConstants.NOT_SET == cost) {
        cost = aPack.getSalePriceOne();
      }
    } else {// ten items
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
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, String key, Map<String, Integer> money,
      int previousSilver, int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__ARMORY_TRANSACTION;
    
    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
  
}
