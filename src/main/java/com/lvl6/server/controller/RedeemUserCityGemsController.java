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
import com.lvl6.events.request.RedeemUserCityGemsRequestEvent;
import com.lvl6.events.response.RedeemUserCityGemsResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.City;
import com.lvl6.info.CityGem;
import com.lvl6.info.User;
import com.lvl6.info.UserCityGem;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RedeemUserCityGemsRequestProto;
import com.lvl6.proto.EventProto.RedeemUserCityGemsResponseProto;
import com.lvl6.proto.EventProto.RedeemUserCityGemsResponseProto.Builder;
import com.lvl6.proto.EventProto.RedeemUserCityGemsResponseProto.RedeemUserCityGemsStatus;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.UserCityGemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityGemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class RedeemUserCityGemsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RedeemUserCityGemsController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RedeemUserCityGemsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REDEEM_USER_CITY_GEMS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RedeemUserCityGemsRequestProto reqProto = ((RedeemUserCityGemsRequestEvent)event)
        .getRedeemUserCityGemsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int cityId = reqProto.getCityId();
    
    RedeemUserCityGemsResponseProto.Builder resBuilder =
        RedeemUserCityGemsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RedeemUserCityGemsStatus.FAIL_OTHER);
    RedeemUserCityGemsResponseEvent resEvent = new RedeemUserCityGemsResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());

    server.lockPlayer(userId, this.getClass().getSimpleName());

    try {
      Map<Integer, CityGem> cityGemIdsToActiveCityGems = 
          CityGemRetrieveUtils.getActiveCityGemIdsToCityGems();
      
      User user = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      //get user city gems
      Map<Integer, UserCityGem> gemIdsToUserCityGems = UserCityGemRetrieveUtils
          .getGemIdsToGemsForUserAndCity(userId, cityId);
      
      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, user,
          userId, cityGemIdsToActiveCityGems, gemIdsToUserCityGems);

      
      boolean success = false;
      int delta = -1;
      if (legitWaitComplete) {
        success = writeChangesToDB(user, userId, cityId, 
            gemIdsToUserCityGems, delta);
      }
      
      if (success) {
        //"buy" the booster packs for the user
        success = redeemForABoosterPack(resBuilder, user, cityId);
      } 

      if (!success){
        //if things go wrong try to undo everything
        resBuilder.setStatus(RedeemUserCityGemsStatus.FAIL_OTHER);
        delta = 1;
        writeChangesToDB(user, userId, cityId, gemIdsToUserCityGems, delta);
      } else {
        resBuilder.setStatus(RedeemUserCityGemsStatus.SUCCESS);
      }

      resEvent.setTag(event.getTag());
      resEvent.setRedeemUserCityGemsResponseProto(resBuilder.build());
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in RedeemUserCityGemsStatusteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder, User user,
      int userId, Map<Integer, CityGem> cityGemIdsToActiveCityGems,
      Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    if (user == null) {
      log.error("unexpected error. null user. userId=" + userId);
      return false;
    }
    
    if (null == cityGemIdsToActiveCityGems || 
        cityGemIdsToActiveCityGems.isEmpty()) {
      log.error("unexpected error: there are no active city gems.");
      return false;
    }
    
    if (null == gemIdsToUserCityGems || gemIdsToUserCityGems.isEmpty()) {
      resBuilder.setStatus(RedeemUserCityGemsStatus.FAIL_INSUFFICIENT_GEMS);
      log.error("user error: user does not have a full set of gems. " +
          "gemIdsToUserCityGems=" + gemIdsToUserCityGems);
      return false;      
    }
    
    //make sure user has at least one of each gem
    boolean userHasAllGemTypes = MiscMethods.doesUserHaveAllGemTypes(
        gemIdsToUserCityGems, cityGemIdsToActiveCityGems);
    if (!userHasAllGemTypes) {
      resBuilder.setStatus(RedeemUserCityGemsStatus.FAIL_INSUFFICIENT_GEMS);
      log.error("user error: user does not have a full set of gems. " +
          "gemIdsToUserCityGems=" + gemIdsToUserCityGems);
      return false;      
    }
    
    resBuilder.setStatus(RedeemUserCityGemsStatus.SUCCESS);
    return true;  
  }

  private boolean writeChangesToDB(User user, int userId, int cityId,
      Map<Integer, UserCityGem> gemIdsToUserCityGems, int delta) {
    Map<Integer, Integer> gemIdsToNewQuantities = 
        newGemQuantities(delta, gemIdsToUserCityGems);
    
    if (!UpdateUtils.get().updateUserCityGems(userId, cityId, gemIdsToNewQuantities)) {
      log.error("unexpected error: could not redeem city gems for user=" +
          user + "\t gemIdsToUserCityGems=" + gemIdsToNewQuantities);
      return false;
    }
    return true;
  }
  
  private Map<Integer, Integer> newGemQuantities (int delta,
      Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    Map<Integer, Integer> gemIdsToNewQuantities = new HashMap<Integer, Integer>();
    
    for (int gemId : gemIdsToUserCityGems.keySet()) {
      UserCityGem ucg = gemIdsToUserCityGems.get(gemId);
      int oldQuantity = ucg.getQuantity();
      int newQuantity = oldQuantity + delta;
      gemIdsToNewQuantities.put(gemId, newQuantity);
    }
    return gemIdsToNewQuantities;
  }
  
  private boolean redeemForABoosterPack(Builder responseBuilder,
      User aUser, int cityId) {
    boolean success = true;
    try {
      //get the booster pack for this city
      City c = CityRetrieveUtils.getCityForCityId(cityId);
      int boosterPackId = c.getBoosterPackId();
      if (ControllerConstants.NOT_SET == boosterPackId) {
        log.error("unexpected error: no booster pack id associated with" +
        		" this city. city=" + c);
        return false;
      }
      int expectedNumEquips = 1;

      //"buy" the booster pack
      Map<Integer, Integer> boosterPackIdsToQuantities = 
          new HashMap<Integer, Integer>();
      boosterPackIdsToQuantities.put(boosterPackId, expectedNumEquips);
      List<Integer> userEquipIds = new ArrayList<Integer>();
      List<FullUserEquipProto> userEquipList = purchaseBoosterPacks(aUser,
          boosterPackIdsToQuantities, userEquipIds);
      
      //error checking
      int size = userEquipList.size();
      if (size != expectedNumEquips) {
        log.error("unexpected error: AW FUCK. Deleting all user equips given." +
            " expectedNumEquips=" + expectedNumEquips + " Actual size=" +
            size + ". userEquipIds=" + userEquipIds);
        DeleteUtils.get().deleteUserEquips(userEquipIds);
        success = false;
      } else {
        //success! give the user his reward
        responseBuilder.addAllEquips(userEquipList);
      }
      
    } catch (Exception e) {
      success = false;
    }
    return success;
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
      
    }
    Map<Integer, Integer> newBoosterItemIdsToNumCollectedCopy =
        new HashMap<Integer, Integer>(newBoosterItemIdsToNumCollected);
    
    //userEquipIds is populated
    boolean successful = writeBoosterStuffToDB(u, newBoosterItemIdsToNumCollectedCopy,
        newBoosterItemIdsToNumCollected, allItemsUserReceives, allCollectedBeforeReset,
        resetOccurred, userEquipIds, now);
    if (successful) {
      recordPurchases(userId, now, packIdToItemsUserReceives, boosterPackIdsToQuantities);
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
      Map<Integer, Integer> boosterPackIdsToQuantities) {
    //this one won't count towards the daily limit
    boolean excludeFromLimitCheck = true;

    for (int boosterPackId : packIdToItemsUserReceives.keySet()) {
      List<BoosterItem> itemsUserReceives = packIdToItemsUserReceives.get(boosterPackId);
      int numBoosterItemsUserWants = boosterPackIdsToQuantities.get(boosterPackId);
      
      MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, boosterPackId,
          numBoosterItemsUserWants, now, itemsUserReceives, excludeFromLimitCheck);
    }
  }
  
  
  
  private boolean writeBoosterStuffToDB(User aUser,
      Map<Integer, Integer> boosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected,
      List<BoosterItem> itemsUserReceives, List<Boolean> collectedBeforeReset,
      boolean resetOccurred, List<Integer> newUserEquipIds, Timestamp now) {
    int userId = aUser.getId();
    List<Integer> userEquipIds = MiscMethods.insertNewUserEquips(userId,
        itemsUserReceives, now);
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
