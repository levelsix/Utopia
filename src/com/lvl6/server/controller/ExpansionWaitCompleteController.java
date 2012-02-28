package com.lvl6.server.controller;

import java.sql.Timestamp;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ExpansionWaitCompleteRequestEvent;
import com.lvl6.events.response.ExpansionWaitCompleteResponseEvent;
import com.lvl6.info.UserCityExpansionData;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteResponseProto.ExpansionWaitCompleteStatus;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityExpansionRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class ExpansionWaitCompleteController extends EventController{

  public ExpansionWaitCompleteController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new ExpansionWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EXPANSION_WAIT_COMPLETE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    ExpansionWaitCompleteRequestProto reqProto = ((ExpansionWaitCompleteRequestEvent)event).getExpansionWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());

    ExpansionWaitCompleteResponseProto.Builder resBuilder = ExpansionWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      UserCityExpansionData userCityExpansionData = UserCityExpansionRetrieveUtils.getUserCityExpansionDataForUser(senderProto.getUserId());
      boolean legitExpansionComplete = checkLegitExpansionComplete(resBuilder, userCityExpansionData, clientTime);

      ExpansionWaitCompleteResponseEvent resEvent = new ExpansionWaitCompleteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setExpansionWaitCompleteResponseProto(resBuilder.build());  

      if (legitExpansionComplete) {
        writeChangesToDB(userCityExpansionData);
      }
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in ExpansionWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(UserCityExpansionData userCityExpansionData) {
    int nearLeftExpansionChange = 0, farLeftExpansionChange = 0, farRightExpansionChange = 0;
    if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.NEAR_LEFT) {
      nearLeftExpansionChange++;
    }
    if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_LEFT) {
      farLeftExpansionChange++;
    }
    if (userCityExpansionData.getLastExpandDirection() == ExpansionDirection.FAR_RIGHT) {
      farRightExpansionChange++;
    }
    if (!UpdateUtils.updateUserExpansionNumexpansionsIsexpanding(userCityExpansionData.getUserId(), 
        nearLeftExpansionChange, farLeftExpansionChange, farRightExpansionChange, false)) {
      log.error("problem with resolving expansion");
    }
  }

  private boolean checkLegitExpansionComplete(Builder resBuilder, UserCityExpansionData userCityExpansionData, Timestamp clientTime) {
    if (userCityExpansionData==null || userCityExpansionData.getLastExpandTime() == null || userCityExpansionData.getLastExpandDirection() == null || clientTime == null) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(clientTime)) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (!userCityExpansionData.isExpanding()) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.WAS_NOT_EXPANDING);
      return false;      
    }
    if (userCityExpansionData.getLastExpandTime().getTime() + 60000*ControllerConstants.EXPANSION_WAIT_COMPLETE__MINUTES_FOR_EXPANSION > clientTime.getTime()) {
      resBuilder.setStatus(ExpansionWaitCompleteStatus.NOT_DONE_YET);
      return false;      
    }
    resBuilder.setStatus(ExpansionWaitCompleteStatus.SUCCESS);
    return true;  
  }
}