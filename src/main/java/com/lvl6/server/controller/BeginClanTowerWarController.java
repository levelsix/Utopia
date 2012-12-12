package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginClanTowerWarRequestEvent;
import com.lvl6.events.response.BeginClanTowerWarResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginClanTowerWarRequestProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.BeginClanTowerWarStatus;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BeginClanTowerWarController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  //For sending messages to online people, NOTIFICATION FEATURE
  @Resource(name = "outgoingGameEventsHandlerExecutor")
  protected TaskExecutor executor;
  public TaskExecutor getExecutor() {
    return executor;
  }
  public void setExecutor(TaskExecutor executor) {
    this.executor = executor;
  }
  @Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;
  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
    return playersByPlayerId;
  }
  public void setPlayersByPlayerId(
      Map<Integer, ConnectedPlayer> playersByPlayerId) {
    this.playersByPlayerId = playersByPlayerId;
  }

  public BeginClanTowerWarController() {
    numAllocatedThreads = 4;
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

    //get the values sent by the client
    MinimumUserProto senderProto = reqProto.getSender();
    int towerId = reqProto.getTowerId();
    boolean claiming = reqProto.getClaiming();
    Timestamp curTime = new Timestamp(reqProto.getCurTime());

    //response to the request setup
    BeginClanTowerWarResponseProto.Builder resBuilder = BeginClanTowerWarResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {

      if(server.lockClanTowersTable()) {
        User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

        Clan clanOfRequester = ClanRetrieveUtils.getClanWithId(senderProto.getClan().getClanId());
        ClanTower oldTower = ClanTowerRetrieveUtils.getClanTower(towerId);
        ClanTower newTower = oldTower.copy();

        int ownerIdBefore = oldTower.getClanOwnerId();
        int attackerIdBefore = oldTower.getClanAttackerId();

        boolean legit = checkLegitBeginClanTowerWarRequest(resBuilder, user, clanOfRequester, 
            claiming, newTower, curTime);

        BeginClanTowerWarResponseEvent resEvent = new BeginClanTowerWarResponseEvent(senderProto.getUserId());
        resEvent.setTag(event.getTag());
        resEvent.setBeginClanTowerWarResponseProto(resBuilder.build());  
        server.writeEvent(resEvent);

        if (legit) {
          int ownerIdAfter = newTower.getClanOwnerId();
          int attackerIdAfter = newTower.getClanAttackerId();

          writeChangesToDB(newTower, oldTower, clanOfRequester, curTime);

          sendClanTowerProtosAndNotifications(ownerIdBefore, ownerIdAfter,
              attackerIdBefore, attackerIdAfter, clanOfRequester, newTower);
        }
      }
    } catch (Exception e) {
      log.error("exception in BeginClanTowerWarController processEvent", e);
    } finally {
      server.unlockClanTowersTable();
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //aTower can be modified to store some new data, as in an owner, or attacker id
  //to be used as a second return value
  private boolean checkLegitBeginClanTowerWarRequest(Builder resBuilder, 
      User user, Clan clanOfRequester, boolean claiming, ClanTower aTower, Timestamp curTime) {
    if (user == null) {
      resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    //check if the tower is valid
    if (null == aTower) {
      //empty tower
      resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
      log.error("tower requested is null.");
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(BeginClanTowerWarStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }

    //check if the request sender is the clan leader or in a clan
    if(null == clanOfRequester || clanOfRequester.getOwnerId() != user.getId()) {
      //non-clan-leader, non-clanned person sent request
      resBuilder.setStatus(BeginClanTowerWarStatus.NOT_CLAN_LEADER);
      log.error("user is not the clan leader or not in a clan. user=" + user);
      return false;
    }

    int clanId = clanOfRequester.getId();
    //check if clan has enough members
    if (ControllerConstants.MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER >
    RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId).size() ){
      //not enough clan members
      resBuilder.setStatus(BeginClanTowerWarStatus.NOT_ENOUGH_CLAN_MEMBERS);
      log.error("clan does not have enough members. clan=" + clanOfRequester);
      return false;
    }

    //check if the clan tower has an owner
    if (ControllerConstants.NOT_SET == aTower.getClanOwnerId() ||
        0 > aTower.getClanOwnerId()) {
      ///no owner for tower, set owner and set empty attacker
      //TODO: FIGURE OUT WHAT STATUS TO RETURN WHEN SETTING THE OWNER OF A TOWER
      //logic here now will suffice
      if(claiming) {
        resBuilder.setStatus(BeginClanTowerWarStatus.SUCCESS);

        aTower.setClanOwnerId(clanId); //set the owner id to use when writing to db
        aTower.setOwnedStartTime(curTime);
        aTower.setOwnerBattleWins(0);

        aTower.setClanAttackerId(ControllerConstants.NOT_SET);
        aTower.setAttackStartTime(null);
        aTower.setAttackerBattleWins(0);

        aTower.setLastRewardGiven(null);
        return true;
      } else {
        resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
        log.error("claiming variable not set when tower has no owner. user="
            + user + ", clan=" + clanOfRequester + ", clanTower=" + aTower);
        return false;
      }
    }

    //check if there already is a clan attacking the tower
    if (ControllerConstants.NOT_SET == aTower.getClanAttackerId() || 
        0 >= aTower.getClanAttackerId()) {
      Clan clanForTower = ClanRetrieveUtils.getClanWithId(aTower.getClanOwnerId());
      boolean towerOwnerClanGood = clanForTower.isGood();
      boolean requesterClanGood = clanOfRequester.isGood();
      boolean oppositeSides = (towerOwnerClanGood && !requesterClanGood)
          || (!towerOwnerClanGood && requesterClanGood);
      
      if(oppositeSides) {
        if(claiming) {
          resBuilder.setStatus(BeginClanTowerWarStatus.TOWER_ALREADY_CLAIMED);

          return false;
        } else {

          //no clan attacking tower, set only the attacker
          //TODO: FIGURE OUT WHAT STATUS TO RETURN WHEN SETTING THE ATTACKER OF A TOWER
          //logic here now will suffice
          resBuilder.setStatus(BeginClanTowerWarStatus.SUCCESS);

          aTower.setClanAttackerId(clanId);
          aTower.setAttackStartTime(curTime);
          aTower.setAttackerBattleWins(0);
          aTower.setOwnerBattleWins(0);
          return true;
        } 
      } else {
        resBuilder.setStatus(BeginClanTowerWarStatus.SAME_SIDE);
        log.error("clans are on the same side (both alliance or both legion");
        return false;
      }
      
    }

    //tower has an owner and an attacker, so deny request
    resBuilder.setStatus(BeginClanTowerWarStatus.TOWER_ALREADY_IN_BATTLE);
    return false;
  }

  private void writeChangesToDB(ClanTower newClanTower, ClanTower oldClanTower, Clan aClan, Timestamp curTime) {
    List<ClanTower> tList = new ArrayList<ClanTower>();
    tList.add(oldClanTower);
    UpdateUtils.get().updateTowerHistory(tList, Notification.CLAN_TOWER_WAR_ENDED);

    if (!UpdateUtils.get().updateClanTowerOwnerAndOrAttacker(
        newClanTower.getId(), 
        newClanTower.getClanOwnerId(), newClanTower.getOwnedStartTime(), newClanTower.getOwnerBattleWins(),
        newClanTower.getClanAttackerId(), newClanTower.getAttackStartTime(), newClanTower.getAttackerBattleWins(),
        newClanTower.getLastRewardGiven())) {
      log.error("problem with updating a clan tower during a BeginClanTowerWarRequest." +
          " clan tower=" + newClanTower +
          " clan=" + aClan +
          " time of request=" + curTime);
    }

  }

  /**
   * Determines which type of tower notification (tower status changed or tower war began) to send.
   * @param ownerBefore - Owner of tower before processing request.
   * @param ownerAfter - Owner of tower after processing request.
   * @param attackerBefore - Attacker of tower before processing request.
   * @param attackerAfter - Attacker of tower after processing request.
   * @param aClan - Need name of tower attacker.
   * @param aTower - Need name of tower.
   */
  private void sendClanTowerProtosAndNotifications(
      int ownerBefore, int ownerAfter, int attackerBefore, int attackerAfter,
      Clan aClan, ClanTower aTower) {

    List<ClanTower> changedTowers = new ArrayList<ClanTower>();
    changedTowers.add(aTower);

    Notification clanTowerWarNotification = new Notification (server, playersByPlayerId.values());
    if (ownerBefore != ownerAfter) {//clan tower owner changed (initialized)

      MiscMethods.sendClanTowerProtosToClient(changedTowers, 
          server, ReasonForClanTowerChange.OWNER_FOR_TOWER_SET);

      clanTowerWarNotification.setNotificationAsClanTowerStatus();
    }
    else if (attackerBefore != attackerAfter) {//clan tower attacker changed (initialized)
      //attackers should be different

      MiscMethods.sendClanTowerProtosToClient(changedTowers, 
          server, ReasonForClanTowerChange.ATTACKER_FOR_TOWER_SET);

      //another db call just for a name...maybe there's a better way to get clan name
      String clanTowerOwnerName = ClanRetrieveUtils.getClanWithId(ownerAfter).getName();
      String clanTowerAttackerName = aClan.getName();
      String towerName = aTower.getTowerName();

      clanTowerWarNotification.setNotificationAsClanTowerWarStarted(clanTowerOwnerName, 
          clanTowerAttackerName, towerName);
    }
    else  {
      log.error("clan tower owner or attacker stayed the same. One of them" +
          "should have been different.");
      return;
    }
    executor.execute(clanTowerWarNotification);
  }
}
