package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveBoosterPackRequestEvent;
import com.lvl6.events.response.RetrieveBoosterPackResponseEvent;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.proto.EventProto.RetrieveBoosterPackRequestProto;
import com.lvl6.proto.EventProto.RetrieveBoosterPackResponseProto;
import com.lvl6.proto.EventProto.RetrieveBoosterPackResponseProto.RetrieveBoosterPackStatus;
import com.lvl6.proto.InfoProto.BoosterPackProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserBoosterPackProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

  @Component @DependsOn("gameServer") public class RetrieveBoosterPackController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveBoosterPackController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveBoosterPackRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_BOOSTER_PACK_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveBoosterPackRequestProto reqProto = ((RetrieveBoosterPackRequestEvent)event).getRetrieveBoosterPackRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    
    RetrieveBoosterPackResponseProto.Builder resBuilder = RetrieveBoosterPackResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveBoosterPackStatus.SOME_FAIL);
    
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      Map<Integer, BoosterPack> packs = BoosterPackRetrieveUtils.getBoosterPackIdsToBoosterPacks();
      if (null == packs || packs.isEmpty()) {
        resBuilder.setStatus(RetrieveBoosterPackStatus.SOME_FAIL);
        log.error("no booster packs available");
      } else {
        //get map of booster pack id to [map of booster item id to booster item]
        Map<Integer, Map<Integer, BoosterItem>> bItemIdsToBItemsForBPackIds = 
            BoosterItemRetrieveUtils.getBoosterItemIdsToBoosterItemsForBoosterPackIds();
        //get map of all booster item ids to quantities for a user
        Map<Integer, Integer> boosterItemIdsToQuantitiesForAUser = 
            UserBoosterItemRetrieveUtils.getBoosterItemIdsToQuantityForUser(userId);

        //protos to insert into builder
        List<BoosterPackProto> boosterPackProtos = new ArrayList<BoosterPackProto>();
        List<UserBoosterPackProto> userBoosterPackProtos = new ArrayList<UserBoosterPackProto>();
        
        for (Integer boosterPackId : packs.keySet()) {
          BoosterPack bp = packs.get(boosterPackId);
          if(!bItemIdsToBItemsForBPackIds.containsKey(boosterPackId)) {
            log.error("booster pack does not have any items associated with it. bp=" + bp);
            continue;
          }
          Map<Integer, BoosterItem> boosterItemIdsToBoosterItems = bItemIdsToBItemsForBPackIds.get(boosterPackId); 
          generateBoosterPackProto(bp, boosterItemIdsToBoosterItems, boosterPackProtos);
         
          generateUserBoosterPackProto(boosterPackId, userId, boosterItemIdsToBoosterItems,
              boosterItemIdsToQuantitiesForAUser, userBoosterPackProtos);
        }
        
        resBuilder.addAllPacks(boosterPackProtos);
        resBuilder.addAllUserPacks(userBoosterPackProtos);
        resBuilder.setStatus(RetrieveBoosterPackStatus.SUCCESS);
      }
      //for each boosterpack, get all the booster items for it
      RetrieveBoosterPackResponseProto resProto = resBuilder.build();

      RetrieveBoosterPackResponseEvent resEvent = new RetrieveBoosterPackResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveBoosterPackResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveBoosterPackController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  public void generateBoosterPackProto(BoosterPack bp, Map<Integer, BoosterItem> boosterItemIdsToBoosterItems, 
      List<BoosterPackProto> boosterPackProtos) {
    BoosterPackProto bpProto = CreateInfoProtoUtils.createBoosterPackProto(bp, boosterItemIdsToBoosterItems.values());
    boosterPackProtos.add(bpProto);
  }
  
  public void generateUserBoosterPackProto(int boosterPackId, int userId, 
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems,  
      Map<Integer, Integer> boosterItemIdsToQuantitiesForAUser,
      List<UserBoosterPackProto> userBoosterPackProtos) {
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
    userBoosterPackProtos.add(ubpProto);
  }
}
