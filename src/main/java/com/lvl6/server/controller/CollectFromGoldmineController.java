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
import com.lvl6.events.request.CollectFromGoldmineRequestEvent;
import com.lvl6.events.response.CollectFromGoldmineResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CollectFromGoldmineRequestProto;
import com.lvl6.proto.EventProto.CollectFromGoldmineResponseProto;
import com.lvl6.proto.EventProto.CollectFromGoldmineResponseProto.Builder;
import com.lvl6.proto.EventProto.CollectFromGoldmineResponseProto.CollectFromGoldmineStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class CollectFromGoldmineController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CollectFromGoldmineController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CollectFromGoldmineRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_COLLECT_FROM_GOLDMINE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    CollectFromGoldmineRequestProto reqProto = ((CollectFromGoldmineRequestEvent)event).getCollectFromGoldmineRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp curTime = new Timestamp(reqProto.getClientTime());

    CollectFromGoldmineResponseProto.Builder resBuilder = CollectFromGoldmineResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegit(resBuilder, user, curTime);

      CollectFromGoldmineResponseEvent resEvent = new CollectFromGoldmineResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCollectFromGoldmineResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
        List<Integer> money = new ArrayList<Integer>();
        writeChangesToDB(user, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, curTime, money);
      }
    } catch (Exception e) {
      log.error("exception in CollectFromGoldmineController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean checkLegit(Builder resBuilder, User user, Timestamp curTime) {
    if (user == null) {
      resBuilder.setStatus(CollectFromGoldmineStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(CollectFromGoldmineStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time =" + curTime + ", servertime~="
          + new Date());
      return false;
    }

    Date lastGoldmineRetrieval = user.getLastGoldmineRetrieval();
    if (lastGoldmineRetrieval == null || lastGoldmineRetrieval.getTime() + 3600000l*(ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL+ControllerConstants.GOLDMINE__NUM_HOURS_TO_PICK_UP) < curTime.getTime()) {
      resBuilder.setStatus(CollectFromGoldmineStatus.NOT_YET_STARTED);
      log.error("timer is not currently going. goldmine retrieval = "+lastGoldmineRetrieval);
      return false;
    }

    long collectTime = lastGoldmineRetrieval.getTime() + 3600000l*ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL;
    if (collectTime > curTime.getTime()) {
      resBuilder.setStatus(CollectFromGoldmineStatus.STILL_COLLECTING);
      log.error("timer is still collecting. goldmine retrieval = "+lastGoldmineRetrieval+" server time = " + new Date());
      return false;
    }

    resBuilder.setStatus(CollectFromGoldmineStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, List<Integer> money) {
    int goldChange = ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP;
    Timestamp stamp = new Timestamp(user.getLastGoldmineRetrieval().getTime() + 3600000l*(ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL+ControllerConstants.GOLDMINE__NUM_HOURS_TO_PICK_UP));
    if (!user.updateLastGoldmineRetrieval(goldChange, stamp)) {
      log.error("problem with adding diamonds for goldmine, adding " + goldChange);
    } else {
      money.add(goldChange);
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
      int currencyBefore = aUser.getDiamonds() - currencyChange;
      String reasonForChange = ControllerConstants.UCHRFC__COLLECT_GOLDMINE;
      int inserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver,
          currencyChange, currencyBefore, reasonForChange);

      log.info("Should be 1. Rows inserted into user_currency_history: " + inserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
}
