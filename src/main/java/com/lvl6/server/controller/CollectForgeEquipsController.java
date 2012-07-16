package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CollectForgeEquipsRequestEvent;
import com.lvl6.events.response.CollectForgeEquipsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CollectForgeEquipsRequestProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto.CollectForgeEquipsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

 @Component @DependsOn("gameServer") public class CollectForgeEquipsController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CollectForgeEquipsController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new CollectForgeEquipsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_COLLECT_FORGE_EQUIPS;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    CollectForgeEquipsRequestProto reqProto = ((CollectForgeEquipsRequestEvent)event).getCollectForgeEquipsRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    int userStructId = reqProto.getUserStructId();
//
//    CollectForgeEquipsResponseProto.Builder resBuilder = CollectForgeEquipsResponseProto.newBuilder();
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
//            resBuilder.setStatus(CollectForgeEquipsStatus.FAIL);
//            log.error("problem with giving user " + diamondChange + " diamonds and " + coinChange + " coins");
//          } else {
//            if (!DeleteUtils.get().deleteUserStruct(userStructId)) {
//              resBuilder.setStatus(CollectForgeEquipsStatus.FAIL);
//              log.error("problem with deleting user struct with user struct id " + userStructId);
//            } else {
//              resBuilder.setStatus(CollectForgeEquipsStatus.SUCCESS);                      
//            }
//          }
//        } else {
//          resBuilder.setStatus(CollectForgeEquipsStatus.FAIL);
//          log.error("parameter null, struct doesn't belong to user, or struct is not complete. user="
//              + user + ", struct=" + struct + ", userStruct=" + userStruct);
//        }
//      } else {
//        resBuilder.setStatus(CollectForgeEquipsStatus.FAIL);       
//        log.error("no user struct with id " + userStructId);
//      }
//
//      CollectForgeEquipsResponseEvent resEvent = new CollectForgeEquipsResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setCollectForgeEquipsResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (user != null) {
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//
//    } catch (Exception e) {
//      log.error("exception in CollectForgeEquips processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());      
//    }
  }
}
