package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseNormStructureRequestEvent;
import com.lvl6.events.response.PurchaseNormStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseNormStructureRequestProto;
import com.lvl6.proto.EventProto.PurchaseNormStructureResponseProto;
import com.lvl6.proto.EventProto.PurchaseNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseNormStructureResponseProto.PurchaseNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;

  @Component @DependsOn("gameServer") public class PurchaseNormStructureController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  public PurchaseNormStructureController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_NORM_STRUCTURE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseNormStructureRequestProto reqProto = ((PurchaseNormStructureRequestEvent)event).getPurchaseNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int structId = reqProto.getStructId();
    CoordinatePair cp = new CoordinatePair(reqProto.getStructCoordinates().getX(), reqProto.getStructCoordinates().getY());
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());

    PurchaseNormStructureResponseProto.Builder resBuilder = PurchaseNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Structure struct = StructureRetrieveUtils.getStructForStructId(structId);

      boolean legitPurchaseNorm = checkLegitPurchaseNorm(resBuilder, struct, user, timeOfPurchase);

      if (legitPurchaseNorm) {
        int userStructId = insertUtils.insertUserStruct(user.getId(), struct.getId(), cp, timeOfPurchase);
        if (userStructId <= 0) {
          legitPurchaseNorm = false;
          resBuilder.setStatus(PurchaseNormStructureStatus.OTHER_FAIL);
          log.error("problem with giving struct " + struct.getId() + " at " + timeOfPurchase + " on " + cp);
        } else {
          resBuilder.setUserStructId(userStructId);
        }
      }

      PurchaseNormStructureResponseEvent resEvent = new PurchaseNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPurchaseNorm) {
        writeChangesToDB(user, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Structure struct) {
    int diamondChange = Math.max(0, struct.getDiamondPrice());
    int coinChange = Math.max(0, struct.getCoinPrice());

    if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondChange*-1, coinChange*-1, 0)) {
      log.error("problem with taking away " + diamondChange + " diamonds, " + coinChange + " coins.");
    }
  }

  private boolean checkLegitPurchaseNorm(Builder resBuilder, Structure struct,
      User user, Timestamp timeOfPurchase) {
    if (user == null || struct == null || timeOfPurchase == null) {
      resBuilder.setStatus(PurchaseNormStructureStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", struct=" + struct 
          + ", timeOfPurchase=" + timeOfPurchase);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(PurchaseNormStructureStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfPurchase + ", servertime~="
          + new Date());
      return false;
    }
    if (user.getLevel() < struct.getMinLevel()) {
      resBuilder.setStatus(PurchaseNormStructureStatus.LEVEL_TOO_LOW);
      log.error("user is too low level to purchase struct. user level=" + user.getLevel() + 
          ", struct's min level is " + struct.getMinLevel());
      return false;
    }
    if (user.getCoins() < struct.getCoinPrice()) {
      resBuilder.setStatus(PurchaseNormStructureStatus.NOT_ENOUGH_MATERIALS);
      log.error("user only has " + user.getCoins() + " coins and needs " + struct.getCoinPrice());
      return false;
    }
    if (user.getDiamonds() < struct.getDiamondPrice()) {
      resBuilder.setStatus(PurchaseNormStructureStatus.NOT_ENOUGH_MATERIALS);
      log.error("user only has " + user.getDiamonds() + " diamonds and needs " + struct.getDiamondPrice());
      return false;
    }

    Map<Integer, List<UserStruct>> structIdsToUserStructs = RetrieveUtils.userStructRetrieveUtils().getStructIdsToUserStructsForUser(user.getId());
    if (structIdsToUserStructs != null) {
      for (Integer structId : structIdsToUserStructs.keySet()) {
        List<UserStruct> userStructsOfSameStructId = structIdsToUserStructs.get(structId);
        if (userStructsOfSameStructId != null) {
          if (structId == struct.getId() && userStructsOfSameStructId.size() >= ControllerConstants.PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE) {
            resBuilder.setStatus(PurchaseNormStructureStatus.ALREADY_HAVE_MAX_OF_THIS_STRUCT);
            log.error("user already has max of this struct, which is " 
                + ControllerConstants.PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE);
            return false;
          }
          for (UserStruct us : userStructsOfSameStructId) {
            if (!us.isComplete() && us.getLastRetrieved() == null) {
              resBuilder.setStatus(PurchaseNormStructureStatus.ANOTHER_STRUCT_STILL_BUILDING);
              log.error("another struct still building: " + us); 
              return false;
            }
          }
        } else {
          log.error("user has no structs? for structid " + structId);
        }
      }
    }
    resBuilder.setStatus(PurchaseNormStructureStatus.SUCCESS);
    return true;
  }
}
