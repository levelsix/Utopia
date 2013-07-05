package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RedeemUserLockBoxItemsRequestEvent;
import com.lvl6.events.response.RedeemUserLockBoxItemsResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.LockBoxItem;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserLockBoxEvent;
import com.lvl6.info.UserLockBoxItem;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsRequestProto;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsResponseProto;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsResponseProto.Builder;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsResponseProto.RedeemUserLockBoxItemsStatus;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.UserLockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.UserLockBoxItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxItemRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class RedeemUserLockBoxItemsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RedeemUserLockBoxItemsController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RedeemUserLockBoxItemsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REDEEM_USER_LOCK_BOX_ITEMS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RedeemUserLockBoxItemsRequestProto reqProto = ((RedeemUserLockBoxItemsRequestEvent)event)
        .getRedeemUserLockBoxItemsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int lockBoxEventId = reqProto.getLockBoxEventId();
    int userId = senderProto.getUserId();
    
    RedeemUserLockBoxItemsResponseProto.Builder resBuilder =
        RedeemUserLockBoxItemsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(userId, this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      //get lock box items for corresponding lock box event
      Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems =
          LockBoxItemRetrieveUtils.getLockBoxIdToLockBoxItemsMapForLockBoxEvent(lockBoxEventId);
      Collection<Integer> lockBoxItemIds = null;
      if (null != lockBoxItemIdsToLockBoxItems && !lockBoxItemIdsToLockBoxItems.isEmpty()){
        lockBoxItemIds = lockBoxItemIdsToLockBoxItems.keySet();
      }
      
      //get user lock box event
      UserLockBoxEvent ulbe = UserLockBoxEventRetrieveUtils.getUserLockBoxEventForUserAndEventId(userId, lockBoxEventId);
      //get user lock box items
      Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems = UserLockBoxItemRetrieveUtils
          .getLockBoxItemIdsToUserLockBoxItemsForUser(userId, lockBoxItemIds);
      
      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, user, lockBoxEventId,
          lockBoxItemIdsToLockBoxItems, ulbe);

      RedeemUserLockBoxItemsResponseEvent resEvent = new RedeemUserLockBoxItemsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      
      boolean success = false;
      boolean redeem = true;
      if (legitWaitComplete) {
        success = writeChangesToDB(user, lockBoxEventId, lockBoxItemIdsToUserLockBoxItems, redeem);
      }
      
      if (success) {
        //"buy" the booster packs for the user
        if (null != lockBoxItemIdsToUserLockBoxItems &&
            !lockBoxItemIdsToUserLockBoxItems.isEmpty()) {
          
          success = writeChangesToBoosterPackHistory(resBuilder, user,
              lockBoxItemIdsToLockBoxItems, lockBoxItemIdsToUserLockBoxItems);
        }
      } 

      if (!success){
        //if things go wrong try to undo everything
        resBuilder.setStatus(RedeemUserLockBoxItemsStatus.FAIL_OTHER);
        redeem = false;
        writeChangesToDB(user, lockBoxEventId, lockBoxItemIdsToUserLockBoxItems, redeem);
      }

      resEvent.setTag(event.getTag());
      resEvent.setRedeemUserLockBoxItemsResponseProto(resBuilder.build());
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in RedeemUserLockBoxItemsStatusteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder, User user,
      int lockBoxEventId, Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems,
      UserLockBoxEvent ulbe) {
    if (user == null || null == ulbe || null == lockBoxItemIdsToLockBoxItems ||
        lockBoxItemIdsToLockBoxItems.isEmpty()) {
      resBuilder.setStatus(RedeemUserLockBoxItemsStatus.FAIL_OTHER);
      log.error("a parameter is null. user=" + user + ", lockBoxItemIdsToLockBoxItems="
      + lockBoxItemIdsToLockBoxItems + ", userLockBoxEvent=" + ulbe);
      return false;
    }
    int currentLockBoxEventId = LockBoxEventRetrieveUtils.getMaxLockBoxEventId();
    if (lockBoxEventId != currentLockBoxEventId) {
      resBuilder.setStatus(RedeemUserLockBoxItemsStatus.FAIL_INVALID_EVENT_ID);
      log.error("unexpected error: client sent weird lockBoxEventId. Expected: " + currentLockBoxEventId +
          ". Actually: " + lockBoxEventId);
      return false;
    }
    
//    boolean hasBeenRedeemed = checkIfUserLockBoxItemsHaveBeenRedeemed(
//        lockBoxItemIdsToUserLockBoxItems);
    if (ulbe.isHasBeenRedeemed()) {
      resBuilder.setStatus(RedeemUserLockBoxItemsStatus.FAIL_ALREADY_REDEEMED);
      log.error("unexpected error: userLockBoxEvent=" + ulbe);
      return false;      
    }
    resBuilder.setStatus(RedeemUserLockBoxItemsStatus.SUCCESS);
    return true;  

  }

//  private boolean checkIfUserLockBoxItemsHaveBeenRedeemed(Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems) {
//    for(int lockBoxItemId : lockBoxItemIdsToUserLockBoxItems.keySet()) {
//      UserLockBoxItem ulbi = lockBoxItemIdsToUserLockBoxItems.get(lockBoxItemId);
//      if (ulbi.isHasBeenRedeemed()) {
//        return true;
//      }
//    }
//    return false;
//  }
  
  private boolean writeChangesToDB(User user, int lockBoxEventId, 
      Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems,
      boolean redeem) {
    
    int userId = user.getId();
    List<Integer> lockBoxItemIds = new ArrayList<Integer>();
    lockBoxItemIds.addAll(lockBoxItemIdsToUserLockBoxItems.keySet());
    
    if (!UpdateUtils.get().updateRedeemLockBoxEvent(lockBoxEventId, userId, redeem)) {
      log.error("unexpected error: could not redeem lock box items for user");
      return false;
    }
    return true;
  }
  
  //return true or false depending on whether purchasing booster packs
  //succeeded or failed
  private boolean writeChangesToBoosterPackHistory(Builder b, User user,
      Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems,
      Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems) {
    
    //booster packs in the database
    Map<Integer, BoosterPack> boosterPackIdToBoosterPacks =
        BoosterPackRetrieveUtils.getBoosterPackIdsToBoosterPacks();
    
    Map<Integer, Integer> boosterPackIdsToQuantities = determineBoosterPackIdsAndQuantities(
        user, lockBoxItemIdsToLockBoxItems, lockBoxItemIdsToUserLockBoxItems,
        boosterPackIdToBoosterPacks);
    
    //log.error("boosterPackIdsToQuantities=" + boosterPackIdsToQuantities);
    
    int numEquipsUserShouldHave = sumUpMapValues(boosterPackIdsToQuantities);
    
    //"buy" booster packs
    List<Integer> userEquipIds = new ArrayList<Integer>();
    List<FullUserEquipProto> userEquipList = purchaseBoosterPacks(user,
        boosterPackIdsToQuantities, userEquipIds);
    
    int size = userEquipList.size(); 
    if (size != numEquipsUserShouldHave) {
      log.error("unexpected error: AW FUCK. Deleting all user equips given." +
      		" numEquipsUserShouldHave=" + numEquipsUserShouldHave + " Actual size=" +
          size + ". userEquipIds=" + userEquipIds);
      DeleteUtils.get().deleteUserEquips(userEquipIds);
      return false;
    } else {
      b.addAllEquips(userEquipList);
      return true;
    }
    
  }
  
  private Map<Integer, Integer> determineBoosterPackIdsAndQuantities(
      User user, Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems,
      Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems,
      Map<Integer, BoosterPack> inDbBoosterPackIdToBoosterPacks) {
    
    int userLevel = user.getLevel();

    List<Integer> silverBoosterPackIds = new ArrayList<Integer>();
    List<Integer> goldBoosterPackIds = new ArrayList<Integer>();
    
    //start filtering the booster packs
    //keep non starter packs and the ones user can buy currently
    getNonStarterPacksAtAndBelowLevel(userLevel, inDbBoosterPackIdToBoosterPacks,
        silverBoosterPackIds, goldBoosterPackIds);
    
    //log.error("silverBoosterPackIds=" + silverBoosterPackIds);
    //log.error("goldBoosterPackIds=" + goldBoosterPackIds);
    
    //determine how many gold and silver equips the user can get
    Map<String, Integer> boosterPackTypeToQuantity = numPacksUserGetsFromLockBoxItems(
        lockBoxItemIdsToLockBoxItems, lockBoxItemIdsToUserLockBoxItems);
    
    //log.error("boosterPackTypeToQuantity=" + boosterPackTypeToQuantity);
    
    //determine the boosterpack id and quantities user gets
    Map<Integer, Integer> boosterPackIdsToQuantities = selectIdsAndQuantities(
        silverBoosterPackIds, goldBoosterPackIds, boosterPackTypeToQuantity);
    //log.error("boosterPackIdsToQuantities=" + boosterPackIdsToQuantities);
    
    return boosterPackIdsToQuantities;
  }
  
  private Map<Integer, BoosterPack> getNonStarterPacksAtAndBelowLevel(int userLevel,
      Map<Integer, BoosterPack> boosterPackIdsToBoosterPacks, List<Integer> silverBoosterPackIds,
      List<Integer> goldBoosterPackIds) {
    Map<Integer, BoosterPack> nonStarterPacks = new HashMap<Integer, BoosterPack>();
    
    for(int boosterPackId : boosterPackIdsToBoosterPacks.keySet()) {
      BoosterPack bp = boosterPackIdsToBoosterPacks.get(boosterPackId);
      if (bp.isStarterPack()) {
        continue;
      }
      
      //keep the ones that can be awarded
      int minLevel = bp.getMinLevel();
      int maxLevel = bp.getMaxLevel();
      if (userLevel >= maxLevel) {
        //booster items below user's level bracket
        nonStarterPacks.put(boosterPackId, bp);
      } else if (maxLevel > userLevel && userLevel >= minLevel) {
        //booster items in user's level bracket
        nonStarterPacks.put(boosterPackId, bp);
      } else {
        continue;
      }
      //keep track of which ones are silver and which ones are gold
      if (bp.isCostsCoins()) {
        silverBoosterPackIds.add(boosterPackId);
      } else {
        goldBoosterPackIds.add(boosterPackId);
      }
      
    }
    return nonStarterPacks;
  }
  
  private Map<String, Integer> numPacksUserGetsFromLockBoxItems(
      Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems,
      Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems) {
    
    Map<String, Integer> boosterPackTypeToQuantity = new HashMap<String, Integer>();
    String silver = MiscMethods.silver;
    String gold = MiscMethods.gold;
    String key = null;
    
    // Initialize with 0
    boosterPackTypeToQuantity.put(silver, 0);
    boosterPackTypeToQuantity.put(gold, 0);
    
    for (int lockBoxItemId : lockBoxItemIdsToUserLockBoxItems.keySet()) {
      UserLockBoxItem ulbi = lockBoxItemIdsToUserLockBoxItems.get(lockBoxItemId);
      LockBoxItem lbi = lockBoxItemIdsToLockBoxItems.get(lockBoxItemId);
      int quantity = ulbi.getQuantity();
      int quantityMultiplier = lbi.getRedeemForNumBoosterItems();
      int newQuantity = quantityMultiplier * quantity;
      
      if (lbi.isGoldBoosterPack()) {
        key = gold;
      } else {
        key = silver;
      }
      
      //running sum, need to keep track of previous amount
      newQuantity += boosterPackTypeToQuantity.get(key);
      boosterPackTypeToQuantity.put(key, newQuantity);
    }
    
    return boosterPackTypeToQuantity;
  }
  
  private Map<Integer, Integer> selectIdsAndQuantities(List<Integer> silverBoosterPackIds,
      List<Integer> goldBoosterPackIds, Map<String, Integer> boosterPackTypeToQuantity) {
    Map<Integer, Integer> returnValue = new HashMap<Integer, Integer>();
    
    int numSilverPacks = boosterPackTypeToQuantity.get(MiscMethods.silver);
    int numGoldPacks = boosterPackTypeToQuantity.get(MiscMethods.gold);
    
    Map<Integer, Integer> silverPackQuantities = MiscMethods.getRandomValues(
        silverBoosterPackIds, numSilverPacks);
    Map<Integer, Integer> goldPackQuantities = MiscMethods.getRandomValues(
        goldBoosterPackIds, numGoldPacks);
    
    returnValue.putAll(silverPackQuantities);
    returnValue.putAll(goldPackQuantities);
    return returnValue;
  }
  
  private int sumUpMapValues(Map<Integer, Integer> stuff) {
    int total = 0;
    for (int i : stuff.keySet()) {
      total += stuff.get(i);
    }
    return total;
  }
  
  //return list of full user equip protos and populate userEquipIds
  private List<FullUserEquipProto> purchaseBoosterPacks(User u,
      Map<Integer, Integer> boosterPackIdsToQuantities, List<Integer> userEquipIds) {
    
    List<FullUserEquipProto> returnValue = new ArrayList<FullUserEquipProto>();
    Timestamp now = new Timestamp((new Date()).getTime());
    int userId = u.getId();
    
    //get all the boosterPacks from db
    Map<Integer, BoosterPack> boosterPacksBeingPurchased = getPacks(boosterPackIdsToQuantities);
    //all the booster items the user has, make only one call and not repeated calls
    Map<Integer, Integer> boosterItemIdsToNumCollectedOld = UserBoosterItemRetrieveUtils
          .getBoosterItemIdsToQuantityForUser(userId);

    //the booster item list that user has after "purchasing"
    Map<Integer, List<BoosterItem>> packIdToItemsUserReceives = new HashMap<Integer, List<BoosterItem>>();
    List<BoosterItem> allItemsUserReceives = new ArrayList<BoosterItem>();
    
    //says if item in itemsUserReceives was collectedBeforeReset
    List<Boolean> allCollectedBeforeReset = new ArrayList<Boolean>();
    Map<Integer, Integer> newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
    boolean resetOccurred = false;
    
    //select all the booster items/equips user gets for each booster pack
    for (int boosterPackId : boosterPackIdsToQuantities.keySet()) {
      int quantity = boosterPackIdsToQuantities.get(boosterPackId);
      BoosterPack aPack = boosterPacksBeingPurchased.get(boosterPackId);
      
      //the items belonging to aPack
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItemsForAPack = BoosterItemRetrieveUtils
          .getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      //taking a subset of boosterItemIdsToNumCollectedOld
      Map<Integer, Integer> boosterItemIdsToNumCollectedForAPack = getBoosterItemsToNumCollected(
          boosterItemIdsToNumCollectedOld, boosterItemIdsToBoosterItemsForAPack);
      
      //the booster item list that user has after "purchasing"
      List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
      List<Boolean> collectedBeforeReset = new ArrayList<Boolean>(); //not really needed
      
      //actually selecting booster items/equips
      resetOccurred = MiscMethods.getAllBoosterItemsForUser(
          boosterItemIdsToBoosterItemsForAPack,
          boosterItemIdsToNumCollectedForAPack,
          quantity, u, aPack, itemsUserReceives,
          collectedBeforeReset) || resetOccurred;
      
      //when recording to db later on, need to distinguish which pack
      //got what items (don't want to create a new method that does that...:P)
      packIdToItemsUserReceives.put(boosterPackId, itemsUserReceives);
      allItemsUserReceives.addAll(itemsUserReceives);
      allCollectedBeforeReset.addAll(collectedBeforeReset);
      
      newBoosterItemIdsToNumCollected.putAll(boosterItemIdsToNumCollectedForAPack);
      
//      //buy single booster pack
//      List<FullUserEquipProto> fuepListTemp = purchaseBoosterPack(aPack,
//          boosterItemIdsToBoosterItemsForAPack, boosterItemIdsToNumCollectedForAPack,
//          u, quantity, now, userEquipIds);
//      
//      if (null == fuepListTemp) {
//        //something went wrong
//        fuepList.clear();
//        break;
//      }
//      //merge it with existing list
//      fuepList.addAll(fuepListTemp);
    }
    Map<Integer, Integer> newBoosterItemIdsToNumCollectedCopy =
        new HashMap<Integer, Integer>(newBoosterItemIdsToNumCollected);
    
    //newUserEquipIds is populated
    boolean successful = writeBoosterStuffToDB(u, newBoosterItemIdsToNumCollectedCopy,
        newBoosterItemIdsToNumCollected, allItemsUserReceives, allCollectedBeforeReset,
        resetOccurred, userEquipIds, now);
    if (successful) {
      recordPurchases(userId, now, packIdToItemsUserReceives,
          boosterPackIdsToQuantities, userEquipIds);
      returnValue = constructFullUserEquipProtos(
          userId, allItemsUserReceives, userEquipIds);
    }
    return returnValue;
  }
  
  
  private Map<Integer, BoosterPack> getPacks(Map<Integer, Integer> boosterPackIdsToQuantities) {
    Collection<Integer> boosterPackIds = boosterPackIdsToQuantities.keySet();
    List<Integer> ids = new ArrayList<Integer>(boosterPackIds);
    
    Map<Integer, BoosterPack> boosterPackIdsToBoosterPacks = 
        BoosterPackRetrieveUtils.getBoosterPacksForBoosterPackIds(ids);
    return boosterPackIdsToBoosterPacks;
  }
  
  //taking a subset of idsToNumCollected
  private Map<Integer, Integer> getBoosterItemsToNumCollected(
      Map<Integer, Integer> idsToNumCollected, Map<Integer, BoosterItem> idsToBoosterItems) {
    Map<Integer, Integer> idsToNumCollectedForItems = new HashMap<Integer, Integer>();
    
    for (int id : idsToBoosterItems.keySet()) {
      int numCollected = 0;
      if (idsToNumCollected.containsKey(id)) {
        numCollected = idsToNumCollected.get(id);
      }
      idsToNumCollectedForItems.put(id, numCollected);
    }
    
    return idsToNumCollectedForItems;
  }
  
  private void recordPurchases(int userId, Timestamp now,
      Map<Integer, List<BoosterItem>> packIdToItemsUserReceives,
      Map<Integer, Integer> boosterPackIdsToQuantities,
      List<Integer> userEquipIds) {
    //this one won't count towards the daily limit
    boolean excludeFromLimitCheck = true;

    for (int boosterPackId : packIdToItemsUserReceives.keySet()) {
      List<BoosterItem> itemsUserReceives = packIdToItemsUserReceives.get(boosterPackId);
      int numBoosterItemsUserWants = boosterPackIdsToQuantities.get(boosterPackId);
      
      MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, boosterPackId,
          numBoosterItemsUserWants, now, itemsUserReceives, excludeFromLimitCheck,
          userEquipIds);
    }
  }
  
  
  //purchase some amount of one booster pack, return the full user equip protos and
  //populate userEquipIds
//  private List<FullUserEquipProto> purchaseBoosterPack(BoosterPack aPack, 
//      Map<Integer, BoosterItem> boosterItemIdsToBoosterItemsForAPack,
//      Map<Integer, Integer> boosterItemIdsToNumCollected, User aUser,
//      int numBoosterItemsUserWants, Timestamp now, List<Integer> userEquipIds) {
//    //return value
//    List<FullUserEquipProto> newUserEquipProtos  = null;
//    
//    //the booster item list that user has after "purchasing"
//    List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
//    //user equip ids generated when recording user "bought" booster packs
//    List<Integer> newUserEquipIds = new ArrayList<Integer>();
//    
//    try {
//      //local vars
//      int userId = aUser.getId();
//      int boosterPackId = aPack.getId();
//      
//      //this is what will be recorded to the db
//      Map<Integer, Integer> newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
//      List<Boolean> collectedBeforeReset = new ArrayList<Boolean>();
//      
//      //actually selecting equips
//      boolean resetOccurred = MiscMethods.getAllBoosterItemsForUser(
//          boosterItemIdsToBoosterItemsForAPack, boosterItemIdsToNumCollected,
//          numBoosterItemsUserWants, aUser, aPack, itemsUserReceives,
//          collectedBeforeReset); 
//      
//      newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>(boosterItemIdsToNumCollected);
//      
//      //newUserEquipIds is populated
//      boolean successful = writeBoosterStuffToDB(aUser, boosterItemIdsToNumCollected,
//          newBoosterItemIdsToNumCollected, itemsUserReceives, collectedBeforeReset,
//          resetOccurred, newUserEquipIds);
//      if (successful) {
//        
//        //this one won't count towards the daily limit
//        boolean excludeFromLimitCheck = true;
//        
//        MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, boosterPackId,
//            numBoosterItemsUserWants, now, itemsUserReceives, excludeFromLimitCheck);
//      } else {
//        return null;
//      }
//      //need to "return" user equip ids
//      userEquipIds.addAll(newUserEquipIds);
//      newUserEquipProtos = constructFullUserEquipProtos(userId,
//          itemsUserReceives, newUserEquipIds);
//      
//    } catch (Exception e) {
//      log.error("unexpected error: ", e);
//      newUserEquipProtos = null;
//    }
//    
//    return newUserEquipProtos; 
//  }
  
  private boolean writeBoosterStuffToDB(User aUser, Map<Integer, Integer> boosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, List<BoosterItem> itemsUserReceives,
      List<Boolean> collectedBeforeReset, boolean resetOccurred, List<Integer> newUserEquipIds,
      Timestamp now) {
    int userId = aUser.getId();
    String reason = ControllerConstants.UER__REDEEM_USER_LOCK_BOX_ITEMS;
    List<Integer> userEquipIds = MiscMethods.insertNewUserEquips(userId, itemsUserReceives,
        now, reason);
    if (null == userEquipIds || userEquipIds.isEmpty() || userEquipIds.size() != itemsUserReceives.size()) {
      log.error("unexpected error: failed to insert equip for user. boosteritems="
          + MiscMethods.shallowListToString(itemsUserReceives));
      return false;
    }

    if (!MiscMethods.updateUserBoosterItems(itemsUserReceives, collectedBeforeReset,
        boosterItemIdsToNumCollected, newBoosterItemIdsToNumCollected, userId, resetOccurred)) {
      // failed to update user_booster_items
      log.error("unexpected error: failed to update user_booster_items for userId: " + userId
          + " attempting to delete equips given: " + MiscMethods.shallowListToString(userEquipIds));
      DeleteUtils.get().deleteUserEquips(userEquipIds);
      return false;
    }
    
    newUserEquipIds.addAll(userEquipIds);
    return true;
  }
  
  private List<FullUserEquipProto> constructFullUserEquipProtos(int userId, 
      List<BoosterItem> biList, List<Integer> userEquipIds) {
    List<FullUserEquipProto> returnValue = new ArrayList<FullUserEquipProto>();
    
    int forgeLevel = ControllerConstants.DEFAULT_USER_EQUIP_LEVEL;
    int enhancementLevel = ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT;
    for (int i = 0; i < biList.size(); i++) {
      BoosterItem bi = biList.get(i);
      int userEquipId = userEquipIds.get(i);
      int equipId = bi.getEquipId();
      
      UserEquip ue = new UserEquip(userEquipId, userId, equipId,
          forgeLevel, enhancementLevel);
      FullUserEquipProto fuep = CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue);
      returnValue.add(fuep);
    }
    return returnValue;
  }
  
}
