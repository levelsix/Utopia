package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
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

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegit(resBuilder, user, curTime);

      CollectFromGoldmineResponseEvent resEvent = new CollectFromGoldmineResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCollectFromGoldmineResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in CollectFromGoldmineController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
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

  private void writeChangesToDB(User user) {
    Timestamp stamp = new Timestamp(user.getLastGoldmineRetrieval().getTime() + 3600000l*(ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL+ControllerConstants.GOLDMINE__NUM_HOURS_TO_PICK_UP));
    if (!user.updateLastGoldmineRetrieval(ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP, stamp)) {
      log.error("problem with adding diamonds for goldmine, adding " + ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP);
    }
  }
}
