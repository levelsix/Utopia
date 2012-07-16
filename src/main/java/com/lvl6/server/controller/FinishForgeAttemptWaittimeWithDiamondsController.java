package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishForgeAttemptWaittimeWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishForgeAttemptWaittimeWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

 @Component @DependsOn("gameServer") public class FinishForgeAttemptWaittimeWithDiamondsController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public FinishForgeAttemptWaittimeWithDiamondsController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new FinishForgeAttemptWaittimeWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FINISH_FORGE_ATTEMPT_WAITTIME_WITH_DIAMONDS;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    FinishForgeAttemptWaittimeWithDiamondsRequestProto reqProto = ((FinishForgeAttemptWaittimeWithDiamondsRequestEvent)event).getFinishForgeAttemptWaittimeWithDiamondsRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    int userStructId = reqProto.getUserStructId();
//
//    FinishForgeAttemptWaittimeWithDiamondsResponseProto.Builder resBuilder = FinishForgeAttemptWaittimeWithDiamondsResponseProto.newBuilder();
//    resBuilder.setSender(senderProto);
//
//    UserStruct userStruct = RetrieveUtils.userStructRetrieveUtils().getSpecificUserStruct(userStructId);
//    Structure struct = null;
//
//    if (userStruct != null) {
//      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
//    }
//
//    server.lockPlayer(senderProto.getUserId());
//
//    try {
//      User user = null;
//      if (userStruct != null) {
//        user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
//        if (user != null && struct != null && user.getId() == userStruct.getUserId()) {
//          int diamondChange = Math.max(0,  (int)Math.ceil(struct.getDiamondPrice()*ControllerConstants.SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER));
//          int coinChange = Math.max(0,  (int)Math.ceil(struct.getCoinPrice()*ControllerConstants.SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER));
//          
//          if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondChange, coinChange, 0)) {
//            resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.FAIL);
//            log.error("problem with giving user " + diamondChange + " diamonds and " + coinChange + " coins");
//          } else {
//            if (!DeleteUtils.get().deleteUserStruct(userStructId)) {
//              resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.FAIL);
//              log.error("problem with deleting user struct with user struct id " + userStructId);
//            } else {
//              resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.SUCCESS);                      
//            }
//          }
//        } else {
//          resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.FAIL);
//          log.error("parameter null, struct doesn't belong to user, or struct is not complete. user="
//              + user + ", struct=" + struct + ", userStruct=" + userStruct);
//        }
//      } else {
//        resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.FAIL);       
//        log.error("no user struct with id " + userStructId);
//      }
//
//      FinishForgeAttemptWaittimeWithDiamondsResponseEvent resEvent = new FinishForgeAttemptWaittimeWithDiamondsResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setFinishForgeAttemptWaittimeWithDiamondsResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (user != null) {
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//
//    } catch (Exception e) {
//      log.error("exception in FinishForgeAttemptWaittimeWithDiamonds processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());      
//    }
  }
}
