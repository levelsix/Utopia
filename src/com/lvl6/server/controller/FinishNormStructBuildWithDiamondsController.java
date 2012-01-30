package com.lvl6.server.controller;

import java.sql.Timestamp;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishNormStructBuildWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishNormStructBuildWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsResponseProto.FinishNormStructBuildStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class FinishNormStructBuildWithDiamondsController extends EventController{

  private static final int DIAMOND_COST_FOR_FINISH_NORM_STRUCT_BUILD = ControllerConstants.FINISH_NORM_STRUCT_BUILD__DIAMOND_COST_FOR_FINISH_NORM_STRUCT_BUILD;
  
  @Override
  public RequestEvent createRequestEvent() {
    return new FinishNormStructBuildWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    
    FinishNormStructBuildWithDiamondsRequestProto reqProto = ((FinishNormStructBuildWithDiamondsRequestEvent)event).getFinishNormStructBuildWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());

    FinishNormStructBuildWithDiamondsResponseProto.Builder resBuilder = FinishNormStructBuildWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());      
      UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
      
      boolean legitBuild = checkLegitBuild(resBuilder, user, userStruct, timeOfPurchase);

      FinishNormStructBuildWithDiamondsResponseEvent resEvent = new FinishNormStructBuildWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setFinishNormStructBuildWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitBuild) {
        writeChangesToDB(user, userStruct, timeOfPurchase);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in FinishNormStructBuildWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Timestamp timeOfPurchase) {
    if (!user.updateRelativeDiamondsNaive(DIAMOND_COST_FOR_FINISH_NORM_STRUCT_BUILD * -1)) {
      log.error("problem with using diamonds to finish norm struct build");
    }
    if (!UpdateUtils.updateUserStructLastretrievedIscomplete(userStruct.getId(), timeOfPurchase, true)) {
      log.error("problem with using diamonds to finish norm struct build");
    }
  }

  private boolean checkLegitBuild(Builder resBuilder, User user, UserStruct userStruct, Timestamp timeOfPurchase) {
    if (user == null || userStruct == null) {
      resBuilder.setStatus(FinishNormStructBuildStatus.OTHER_FAIL);
      return false;
    }
    if (timeOfPurchase.getTime() < userStruct.getPurchaseTime().getTime()) {
      resBuilder.setStatus(FinishNormStructBuildStatus.OTHER_FAIL);
      return false;
    }
    if (user.getDiamonds() < DIAMOND_COST_FOR_FINISH_NORM_STRUCT_BUILD) {
      resBuilder.setStatus(FinishNormStructBuildStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    resBuilder.setStatus(FinishNormStructBuildStatus.SUCCESS);
    return true;  
  }
}
