package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SubmitEquipsToBlacksmithRequestEvent;
import com.lvl6.events.response.SubmitEquipsToBlacksmithResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithRequestProto;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto.SubmitEquipsToBlacksmithStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

 @Component @DependsOn("gameServer") public class SubmitEquipsToBlacksmithController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public SubmitEquipsToBlacksmithController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new SubmitEquipsToBlacksmithRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SUBMIT_EQUIPS_TO_BLACKSMITH;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    SubmitEquipsToBlacksmithRequestProto reqProto = ((SubmitEquipsToBlacksmithRequestEvent)event).getSubmitEquipsToBlacksmithRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    int userStructId = reqProto.getUserStructId();
//
//    SubmitEquipsToBlacksmithResponseProto.Builder resBuilder = SubmitEquipsToBlacksmithResponseProto.newBuilder();
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
//            resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FAIL);
//            log.error("problem with giving user " + diamondChange + " diamonds and " + coinChange + " coins");
//          } else {
//            if (!DeleteUtils.get().deleteUserStruct(userStructId)) {
//              resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FAIL);
//              log.error("problem with deleting user struct with user struct id " + userStructId);
//            } else {
//              resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.SUCCESS);                      
//            }
//          }
//        } else {
//          resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FAIL);
//          log.error("parameter null, struct doesn't belong to user, or struct is not complete. user="
//              + user + ", struct=" + struct + ", userStruct=" + userStruct);
//        }
//      } else {
//        resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FAIL);       
//        log.error("no user struct with id " + userStructId);
//      }
//
//      SubmitEquipsToBlacksmithResponseEvent resEvent = new SubmitEquipsToBlacksmithResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setSubmitEquipsToBlacksmithResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (user != null) {
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//
//    } catch (Exception e) {
//      log.error("exception in SubmitEquipsToBlacksmith processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());      
//    }
  }
}
