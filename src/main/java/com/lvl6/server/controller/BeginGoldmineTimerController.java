package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginGoldmineTimerRequestEvent;
import com.lvl6.events.response.BeginGoldmineTimerResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginGoldmineTimerRequestProto;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto.BeginGoldmineTimerStatus;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class BeginGoldmineTimerController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegit(resBuilder, user, curTime, reset);

      BeginGoldmineTimerResponseEvent resEvent = new BeginGoldmineTimerResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setBeginGoldmineTimerResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
        List<Integer> money = new ArrayList<Integer>();
        writeChangesToDB(user, curTime, reset, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, curTime, money);
      }
    } catch (Exception e) {
      log.error("exception in BeginGoldmineTimerController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
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
      long collectTime = lastGoldmineRetrieval.getTime() + 3600000l*ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL;
      if (!reset && collectTime < curTime.getTime()) {
        resBuilder.setStatus(BeginGoldmineTimerStatus.STILL_COLLECTING);
        log.error("timer is not currently going. goldmine retrieval = "+lastGoldmineRetrieval);
        return false;
      }
    }

    resBuilder.setStatus(BeginGoldmineTimerStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, Timestamp curTime, boolean reset, List<Integer> money) {
    int goldCost = reset ? ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART : 0;
    Timestamp newStamp = reset ? null : curTime;
    if (!user.updateLastGoldmineRetrieval(-goldCost, newStamp)) {
      log.error("problem with adding diamonds for goldmine, adding " + ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP);
    } else {
      if(reset) {
        money.add(-goldCost);
      }
    }
  }
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, List<Integer> money) {
    try {
      if(money.isEmpty()) {
        return;
      }
      int userId = aUser.getId();
      int isSilver = 0;
      int currencyChange = money.get(0);
      int currencyAfter = aUser.getDiamonds();
      int currencyBefore = currencyAfter - currencyChange;
      String reasonForChange = ControllerConstants.UCHRFC__GOLDMINE;
      InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver,
          currencyChange, currencyBefore, currencyAfter, reasonForChange);

      //log.info("Should be 1. Rows inserted into user_currency_history: " + inserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
}
