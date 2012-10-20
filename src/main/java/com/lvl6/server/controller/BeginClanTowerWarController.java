package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginClanTowerWarRequestEvent;
import com.lvl6.events.response.BeginClanTowerWarResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginClanTowerWarRequestProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.BeginClanTowerWarStatus;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class BeginClanTowerWarController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public BeginClanTowerWarController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BeginClanTowerWarRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BEGIN_CLAN_TOWER_WAR;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    BeginClanTowerWarRequestProto reqProto = ((BeginClanTowerWarRequestEvent)event).getBeginClanTowerWarRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    BeginClanTowerWarResponseProto.Builder resBuilder = BeginClanTowerWarResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    Clan clan = ClanRetrieveUtils.getClanWithId(senderProto.getClan().getClanId());

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegit(resBuilder, user, curTime, reset);

      BeginClanTowerWarResponseEvent resEvent = new BeginClanTowerWarResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setBeginClanTowerWarResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
        writeChangesToDB(user, curTime, reset);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in BeginClanTowerWarController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegit(Builder resBuilder, User user, Timestamp curTime, boolean reset) {
    if (user == null) {
      resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(BeginClanTowerWarStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }

    Date lastGoldmineRetrieval = user.getLastGoldmineRetrieval();
    if (lastGoldmineRetrieval == null && reset) {
      resBuilder.setStatus(BeginClanTowerWarStatus.NOT_ENOUGH_DIAMONDS);
      log.error("trying to reset goldmine when it has never been set");
      return false;
    }
    if (lastGoldmineRetrieval != null && user.getDiamonds() < ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART) {
      resBuilder.setStatus(BeginClanTowerWarStatus.NOT_ENOUGH_DIAMONDS);
      log.error("not enough diamonds to restart goldmine. current diamonds = "+user.getDiamonds());
      return false;
    }
    if (lastGoldmineRetrieval != null) {
      long collectTime = lastGoldmineRetrieval.getTime() + 3600000l*ControllerConstants.GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL;
      if (!reset && collectTime < curTime.getTime()) {
        resBuilder.setStatus(BeginClanTowerWarStatus.STILL_COLLECTING);
        log.error("timer is not currently going. goldmine retrieval = "+lastGoldmineRetrieval);
        return false;
      }
    }

    resBuilder.setStatus(BeginClanTowerWarStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, Timestamp curTime, boolean reset) {
    int goldCost = reset ? ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART : 0;
    Timestamp newStamp = reset ? null : curTime;
    if (!user.updateLastGoldmineRetrieval(-goldCost, newStamp)) {
      log.error("problem with adding diamonds for goldmine, adding " + ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP);
    }
  }
}
