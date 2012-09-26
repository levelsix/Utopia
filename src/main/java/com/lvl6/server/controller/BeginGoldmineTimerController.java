package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginGoldmineTimerRequestEvent;
import com.lvl6.events.response.BeginGoldmineTimerResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginGoldmineTimerRequestProto;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto.Builder;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto.BeginGoldmineTimerStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class BeginGoldmineTimerController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public BeginGoldmineTimerController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BeginGoldmineTimerRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BEGIN_GOLDMINE_TIMER_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    BeginGoldmineTimerRequestProto reqProto = ((BeginGoldmineTimerRequestEvent)event).getBeginGoldmineTimerRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp curTime = new Timestamp(reqProto.getClientTime());
    boolean reset = reqProto.getReset();

    BeginGoldmineTimerResponseProto.Builder resBuilder = BeginGoldmineTimerResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegit(resBuilder, user, curTime, reset);

      BeginGoldmineTimerResponseEvent resEvent = new BeginGoldmineTimerResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setBeginGoldmineTimerResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
        writeChangesToDB(user, curTime, reset);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in BeginGoldmineTimerController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegit(Builder resBuilder, User user, Timestamp curTime, boolean reset) {
    if (user == null) {
      resBuilder.setStatus(BeginGoldmineTimerStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(BeginGoldmineTimerStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }

    Date lastGoldmineRetrieval = user.getLastGoldmineRetrieval();
    if (lastGoldmineRetrieval == null && reset) {
      resBuilder.setStatus(BeginGoldmineTimerStatus.NOT_ENOUGH_DIAMONDS);
      log.error("trying to reset goldmine when it has never been set");
      return false;
    }
    if (lastGoldmineRetrieval != null && user.getDiamonds() < ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART) {
      resBuilder.setStatus(BeginGoldmineTimerStatus.NOT_ENOUGH_DIAMONDS);
      log.error("not enough diamonds to restart goldmine. current diamonds = "+user.getDiamonds());
      return false;
    }
    if (lastGoldmineRetrieval != null) {
      long collectTime = lastGoldmineRetrieval.getTime() + 60000*ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL;
      if (!reset && collectTime < curTime.getTime()) {
        resBuilder.setStatus(BeginGoldmineTimerStatus.STILL_COLLECTING);
        log.error("timer is not currently going. goldmine retrieval = "+lastGoldmineRetrieval);
        return false;
      }
    }

    resBuilder.setStatus(BeginGoldmineTimerStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, Timestamp curTime, boolean reset) {
    int goldCost = reset ? ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART : 0;
    if (!user.updateLastGoldmineRetrieval(-goldCost, curTime)) {
      log.error("problem with adding diamonds for goldmine, adding " + ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP);
    }
  }
}
