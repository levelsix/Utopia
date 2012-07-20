package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishForgeAttemptWaittimeWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishForgeAttemptWaittimeWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto.FinishForgeAttemptWaittimeWithDiamondsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class FinishForgeAttemptWaittimeWithDiamondsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public FinishForgeAttemptWaittimeWithDiamondsController() {
    numAllocatedThreads = 2;
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

    FinishForgeAttemptWaittimeWithDiamondsRequestProto reqProto = ((FinishForgeAttemptWaittimeWithDiamondsRequestEvent)event).getFinishForgeAttemptWaittimeWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int blacksmithId = reqProto.getBlacksmithId();
    Timestamp timeOfSpeedup = new Timestamp(reqProto.getTimeOfSpeedup());

    FinishForgeAttemptWaittimeWithDiamondsResponseProto.Builder resBuilder = FinishForgeAttemptWaittimeWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser = UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(senderProto.getUserId());
      
      boolean legitFinish = checkLegitFinish(resBuilder, blacksmithId, unhandledBlacksmithAttemptsForUser, user, timeOfSpeedup);

      FinishForgeAttemptWaittimeWithDiamondsResponseEvent resEvent = new FinishForgeAttemptWaittimeWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setFinishForgeAttemptWaittimeWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitFinish) {
        BlacksmithAttempt ba = unhandledBlacksmithAttemptsForUser.get(0);
        
        int diamondCost = calculateDiamondCostToSpeedupForgeWaittime(EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(ba.getEquipId()), ba.getGoalLevel());
        writeChangesToDB(user, ba, timeOfSpeedup, diamondCost);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in FinishForgeAttemptWaittimeWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, BlacksmithAttempt ba, Timestamp timeOfSpeedup, int diamondCost) {
    if (!UpdateUtils.get().updateAbsoluteBlacksmithAttemptcompleteTimeofspeedup(ba.getId(), timeOfSpeedup, true)) {
      log.error("problem with updating blacksmith attempt complete and time of speedup. ba=" + ba + ", timeOfSpeedup is " + timeOfSpeedup + ", attempt complete is true");
    }    
    if (diamondCost > 0) {
      if (!user.updateRelativeDiamondsNaive(diamondCost*-1)) {
        log.error("problem with taking away diamonds post forge speedup, taking away " + diamondCost + ", user only has " + user.getDiamonds());
      }
    }
  }

  private boolean checkLegitFinish(Builder resBuilder, int blacksmithId,
      List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser, User user,
      Timestamp timeOfSpeedup) {
    if (unhandledBlacksmithAttemptsForUser == null || user == null || unhandledBlacksmithAttemptsForUser.size() != 1 || timeOfSpeedup == null) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.OTHER_FAIL);
      log.error("a parameter passed in is null or invalid. unhandledBlacksmithAttemptsForUser= " + unhandledBlacksmithAttemptsForUser + ", user= " + user
          + ", timeOfSpeedup=" + timeOfSpeedup);
      return false;
    }
    
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfSpeedup)) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfSpeedup + ", servertime~="
          + new Date());
      return false;
    }
    
    BlacksmithAttempt blacksmithAttempt = unhandledBlacksmithAttemptsForUser.get(0);
    Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId());

    if (blacksmithAttempt.isAttemptComplete()) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.ALREADY_COMPLETE);
      log.error("user trying to speed up arleady complete forge attempt: " + blacksmithAttempt);
      return false;
    }

    if (blacksmithAttempt.getUserId() != user.getId() || blacksmithAttempt.getId() != blacksmithId || equip == null) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.OTHER_FAIL);
      log.error("wrong blacksmith attempt. blacksmith attempt is " + blacksmithAttempt + ", blacksmith id passed in is " + blacksmithId + ", equip = " + equip);
      return false;
    }
    
    if (blacksmithAttempt.getStartTime().getTime() > timeOfSpeedup.getTime()) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.OTHER_FAIL);
      log.error("start time after speedup time. starttime = " + blacksmithAttempt.getStartTime() + ", speedup time is " + timeOfSpeedup);
      return false;
    }


    int diamondCost = calculateDiamondCostToSpeedupForgeWaittime(equip, blacksmithAttempt.getGoalLevel());
    if (user.getDiamonds() < diamondCost) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user doesn't have enough diamonds. has " + user.getDiamonds() +", needs " + diamondCost);
      return false;
    }
    resBuilder.setStatus(FinishForgeAttemptWaittimeWithDiamondsStatus.SUCCESS);
    return true;  
  }

  private int calculateDiamondCostToSpeedupForgeWaittime(Equipment equipment, int goalLevel) {
    return (int) MiscMethods.calculateMinutesToFinishForgeAttempt(equipment, goalLevel) / 
        (ControllerConstants.FORGE_BASE_MINUTES_TO_ONE_GOLD+equipment.getMinLevel()/ControllerConstants.AVERAGE_SIZE_OF_LEVEL_BRACKET);
  }

}