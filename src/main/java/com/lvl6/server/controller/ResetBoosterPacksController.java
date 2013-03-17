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
import com.lvl6.events.request.ResetBoosterPackRequestEvent;
import com.lvl6.events.response.ResetBoosterPacksResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.ResetBoosterPackRequestProto;
import com.lvl6.proto.EventProto.ResetBoosterPackResponseProto;
import com.lvl6.proto.EventProto.ResetBoosterPackResponseProto.Builder;
import com.lvl6.proto.EventProto.ResetBoosterPackResponseProto.ResetBoosterPackStatus;
import com.lvl6.proto.InfoProto.BoosterPackProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserBoosterPackProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class ResetBoosterPacksController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ResetBoosterPacksController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ResetBoosterPackRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RESET_BOOSTER_PACK_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ResetBoosterPackRequestProto reqProto = ((ResetBoosterPackRequestEvent)event).getResetBoosterPackRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int boosterPackId = reqProto.getBoosterPackId();
    
    ResetBoosterPackResponseProto.Builder resBuilder = ResetBoosterPackResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(ResetBoosterPackStatus.OTHER_FAIL);
    
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      BoosterPack aPack = BoosterPackRetrieveUtils.getBoosterPackForBoosterPackId(boosterPackId);
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems = BoosterItemRetrieveUtils.getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      //get map of all booster item ids to quantities for a user
      Map<Integer, Integer> boosterItemIdsToQuantitiesForAUser = 
          UserBoosterItemRetrieveUtils.getBoosterItemIdsToQuantityForUser(userId);

      boolean legitReset = checkLegitReset(resBuilder, aPack, boosterItemIdsToBoosterItems);
      boolean success = false;
      Map<Integer, Integer> clientNewBoosterItemIdsToQuantitiesForUser = new HashMap<Integer, Integer>();
      Map<Integer, Integer> dbNewBoosterItemIdsToQuantitiesForUser = new HashMap<Integer, Integer>();
      if (legitReset) {
        //reset ONLY the booster item quantities for the booster pack user requested
        clientNewBoosterItemIdsToQuantitiesForUser = resetQuantities(boosterItemIdsToQuantitiesForAUser, 
            boosterItemIdsToBoosterItems, dbNewBoosterItemIdsToQuantitiesForUser);
        success = writeChangesToDB(resBuilder, userId, dbNewBoosterItemIdsToQuantitiesForUser);
      }
      
      if (success) {
        //proto to insert into builder
        UserBoosterPackProto ubpp = generateUserBoosterPackProto(
            boosterPackId, userId, boosterItemIdsToBoosterItems, clientNewBoosterItemIdsToQuantitiesForUser);
        resBuilder.setUserBoosterPack(ubpp);
      }

      ResetBoosterPackResponseProto resProto = resBuilder.build();
      ResetBoosterPacksResponseEvent resEvent = new ResetBoosterPacksResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setResetBoosterPackResponseProto(resProto);
      server.writeEvent(resEvent);
      
      if (success) {
        writeToUserBoosterPackHistory(userId, boosterPackId);
      }
    } catch (Exception e) {
      log.error("exception in ResetBoosterPacksController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }
  
  private boolean checkLegitReset(Builder resBuilder, BoosterPack aPack,
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems) {
    if (null == aPack || null == boosterItemIdsToBoosterItems 
        || boosterItemIdsToBoosterItems.isEmpty()) {
      resBuilder.setStatus(ResetBoosterPackStatus.OTHER_FAIL);
      log.error("BoosterPack might null or the BoosterPack has no BoosterItems. pack:"
          + aPack + ", boosterItemIdsToBoosterItems:" + boosterItemIdsToBoosterItems);
      return false;
    }
    resBuilder.setStatus(ResetBoosterPackStatus.SUCCESS);
    return true;
  }

  private Map<Integer, Integer> resetQuantities(Map<Integer, Integer> boosterItemIdsQuantitiesForUser,
     Map<Integer, BoosterItem> boosterItemIdsToBoosterItems, Map<Integer, Integer> dbNewBoosterItemIdsToQuantitiesForUser) {
    
    Map<Integer, Integer> clientNewBoosterItemIdsToQuantitiesForUser = new HashMap<Integer, Integer>();
    //for each user booster item, check if the item belongs in the booster items for a pack
    for (int boosterItemId : boosterItemIdsQuantitiesForUser.keySet()) {
      
      if (boosterItemIdsToBoosterItems.containsKey(boosterItemId)) {
        dbNewBoosterItemIdsToQuantitiesForUser.put(boosterItemId, 0);
        clientNewBoosterItemIdsToQuantitiesForUser.put(boosterItemId, 0);
      } else {
        //the client needs a copy of all the user booster items the user has
        int quantity = boosterItemIdsQuantitiesForUser.get(boosterItemId);
        clientNewBoosterItemIdsToQuantitiesForUser.put(boosterItemId, quantity);
      }
    }
    return clientNewBoosterItemIdsToQuantitiesForUser;
  }
  
  private boolean writeChangesToDB(Builder resBuilder, int userId,
      Map<Integer, Integer> boosterItemIdsToQuantitiesForUser) {
    return UpdateUtils.get().updateUserBoosterItemsForOneUser(userId, boosterItemIdsToQuantitiesForUser);
  }
  
  public void generateBoosterPackProto(BoosterPack bp, Map<Integer, BoosterItem> boosterItemIdsToBoosterItems, 
      List<BoosterPackProto> boosterPackProtos) {
    BoosterPackProto bpProto = CreateInfoProtoUtils.createBoosterPackProto(bp, boosterItemIdsToBoosterItems.values());
    boosterPackProtos.add(bpProto);
  }
  
  public UserBoosterPackProto generateUserBoosterPackProto(int boosterPackId, int userId, 
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems,  
      Map<Integer, Integer> boosterItemIdsToQuantitiesForAUser) {
    Map<Integer, Integer> bItemIdToQuantity = new HashMap<Integer, Integer>();
    
    //for each booster item in a pack, check if the user has any
    for(Integer bItemId : boosterItemIdsToBoosterItems.keySet()) {
      if(!boosterItemIdsToQuantitiesForAUser.containsKey(bItemId)) {
        //log.info("user does not have booster item: " + boosterItemIdsToBoosterItems.get(bItemId));
        continue;
      }
      int quantity = boosterItemIdsToQuantitiesForAUser.get(bItemId);
      bItemIdToQuantity.put(bItemId, quantity);
    }
    UserBoosterPackProto ubpProto = 
        CreateInfoProtoUtils.createUserBoosterPackProto(boosterPackId, userId, bItemIdToQuantity);
    return ubpProto;
  }
  
  private void writeToUserBoosterPackHistory(int userId, int packId) {
    Timestamp nowTimestamp = new Timestamp((new Date()).getTime());
    int numBought = 0; //indicates a reset
    MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, packId, numBought, nowTimestamp,
        new ArrayList<BoosterItem>());
  }
}
