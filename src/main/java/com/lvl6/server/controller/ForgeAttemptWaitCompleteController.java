package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ForgeAttemptWaitCompleteRequestEvent;
import com.lvl6.events.response.ForgeAttemptWaitCompleteResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteResponseProto.ForgeAttemptWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ForgeAttemptWaitCompleteController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ForgeAttemptWaitCompleteController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ForgeAttemptWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FORGE_ATTEMPT_WAIT_COMPLETE;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ForgeAttemptWaitCompleteRequestProto reqProto = ((ForgeAttemptWaitCompleteRequestEvent)event).getForgeAttemptWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int blacksmithId = reqProto.getBlacksmithId();
    Timestamp curTime = new Timestamp(reqProto.getCurTime());

    ForgeAttemptWaitCompleteResponseProto.Builder resBuilder = ForgeAttemptWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser = UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(senderProto.getUserId());

      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, blacksmithId, unhandledBlacksmithAttemptsForUser, user, curTime);

      ForgeAttemptWaitCompleteResponseEvent resEvent = new ForgeAttemptWaitCompleteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setForgeAttemptWaitCompleteResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitWaitComplete) {
        writeChangesToDB(unhandledBlacksmithAttemptsForUser.get(0));
      }
    } catch (Exception e) {
      log.error("exception in ForgeAttemptWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(BlacksmithAttempt ba) {
    if (!UpdateUtils.get().updateAbsoluteBlacksmithAttemptcompleteTimeofspeedup(ba.getId(), null, true)) {
      log.error("problem with updating blacksmith attempt complete and time of speedup. ba=" + ba + ", timeOfSpeedup is null, attempt complete is true");
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder, int blacksmithId,
      List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser, User user,
      Timestamp curTime) {
    
    if (unhandledBlacksmithAttemptsForUser == null || user == null || curTime == null || unhandledBlacksmithAttemptsForUser.size() != 1) {
      resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.OTHER_FAIL);
      log.error("a parameter passed in is null or invalid. unhandledBlacksmithAttemptsForUser= " + unhandledBlacksmithAttemptsForUser + ", user= " + user
          + ", curTime=" + curTime);
      return false;
    }
    
    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }
    
    BlacksmithAttempt blacksmithAttempt = unhandledBlacksmithAttemptsForUser.get(0);
    Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId());

    if (blacksmithAttempt.isAttemptComplete()) {
      resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.ALREADY_COMPLETE);
      log.error("user already has a unhandled complete forge: " + blacksmithAttempt);
      return false;
    }
    
    if (blacksmithAttempt.getUserId() != user.getId() || blacksmithAttempt.getId() != blacksmithId || equip == null) {
      resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.OTHER_FAIL);
      log.error("wrong blacksmith attempt. blacksmith attempt is " + blacksmithAttempt + ", blacksmith id passed in is " + blacksmithId + ", equip = " + equip);
      return false;
    }
    
    if (blacksmithAttempt.getStartTime().getTime() + 60000*MiscMethods.calculateMinutesToFinishForgeAttempt(equip, blacksmithAttempt.getGoalLevel())  > curTime.getTime()) {
      resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.NOT_DONE_YET);
      log.error("the forging is not done yet. blacksmithattempt=" + blacksmithAttempt + ", and minutes to finish forging is "
          + MiscMethods.calculateMinutesToFinishForgeAttempt(equip, blacksmithAttempt.getGoalLevel())
          + ", client time is " + curTime);
      return false;
    }
    resBuilder.setStatus(ForgeAttemptWaitCompleteStatus.SUCCESS);
    return true;  
  }

}
