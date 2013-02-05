package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ConcedeClanTowerWarRequestEvent;
import com.lvl6.events.response.ConcedeClanTowerWarResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarRequestProto;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarResponseProto;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarResponseProto.Builder;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarResponseProto.ConcedeClanTowerWarStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ConcedeClanTowerWarController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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


  public ConcedeClanTowerWarController() {
    numAllocatedThreads = 4;
  }


  @Override
  public RequestEvent createRequestEvent() {
    return new ConcedeClanTowerWarRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CONCEDE_CLAN_TOWER_WAR;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    ConcedeClanTowerWarRequestProto reqProto = ((ConcedeClanTowerWarRequestEvent)event).getConcedeClanTowerWarRequestProto();

    //get the values sent by the client
    MinimumUserProto senderProto = reqProto.getSender();
    int towerId = reqProto.getTowerId();
    Timestamp curTime = new Timestamp(reqProto.getCurTime());

    //response to the request setup
    ConcedeClanTowerWarResponseProto.Builder resBuilder = ConcedeClanTowerWarResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    if(server.lockClanTowersTable()) {
	    try {
	        User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
	
	        ClanTower oldTower = ClanTowerRetrieveUtils.getClanTower(towerId);
	        ClanTower newTower = oldTower.copy();
	        Clan oldClanTowerAttacker = 
	            ClanRetrieveUtils.getClanWithId(newTower.getClanAttackerId()); //newTower, oldTower doesn't matter
	        Clan oldClanTowerOwner = 
	            ClanRetrieveUtils.getClanWithId(newTower.getClanOwnerId()); //newTower, oldTower doesn't matter
	
	        boolean legit = checkLegitConcedeClanTowerWarRequest(
	            resBuilder, user, oldClanTowerAttacker, oldClanTowerOwner, newTower, curTime);
	
	        ConcedeClanTowerWarResponseEvent resEvent = new ConcedeClanTowerWarResponseEvent(senderProto.getUserId());
	        resEvent.setTag(event.getTag());
	        resEvent.setConcedeClanTowerWarResponseProto(resBuilder.build());  
	        server.writeEvent(resEvent);
	
	        if (legit) {
	          writeChangesToDB(oldTower, newTower, curTime, user);
	
	          sendTowersAndNotifications(oldTower.getClanOwnerId(), newTower.getClanAttackerId(), 
	              newTower, oldClanTowerAttacker, oldClanTowerOwner);
	        }
	    } catch (Exception e) {
	      log.error("exception in BeginClanTowerWarController processEvent", e);
	    } finally {
	      server.unlockClanTowersTable();
	      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
	    }
    }else {
    	log.error("Failed to obtain lock on Clantowers table");
    }
  }

  //aTower can be modified to store some new data, as in an owner, or attacker id
  //to be used as a second return value
  private boolean checkLegitConcedeClanTowerWarRequest(Builder resBuilder, User user, 
      Clan oldClanTowerAttacker, Clan oldClanTowerOwner, ClanTower aTower, Timestamp curTime) {
    if (user == null) {
      resBuilder.setStatus(ConcedeClanTowerWarStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    //check if the tower is valid
    if (null == aTower || null == oldClanTowerOwner || null == oldClanTowerAttacker) {
      //empty tower
      resBuilder.setStatus(ConcedeClanTowerWarStatus.OTHER_FAIL);
      log.error("tower requested is null, or clan tower owner or attacker is null. aTower=" +
          aTower + " oldClanTowerOwner=" + oldClanTowerOwner + " oldClanTowerAttacker=" + oldClanTowerAttacker);
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(ConcedeClanTowerWarStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }

    int clanOfUser = user.getClanId();
    //check if user is in clan owning or attacking the tower
    if (oldClanTowerAttacker.getId() != clanOfUser &&
        oldClanTowerOwner.getId() != clanOfUser) {
      //user is not owner or attacker
      resBuilder.setStatus(ConcedeClanTowerWarStatus.NOT_CLAN_TOWER_WAR_PARTICIPANT);
      log.error("user is not a clan tower owner or attacker.");
      return false;
    }

    int oldClanTowerOwnerClanOwnerId = oldClanTowerOwner.getOwnerId();
    int oldClanTowerAttackerClanOwnerId = oldClanTowerAttacker.getOwnerId();
    int userId = user.getId();

    //check if the request sender is the clan leader
    if(oldClanTowerAttackerClanOwnerId == userId) {
      //tower owner wins
      aTower.setClanAttackerId(ControllerConstants.NOT_SET);
      aTower.setAttackerBattleWins(0);
      aTower.setAttackStartTime(null);
      resBuilder.setStatus(ConcedeClanTowerWarStatus.SUCCESS);
      return true;
    }

    if(oldClanTowerOwnerClanOwnerId == userId) {
      if (0 > oldClanTowerAttackerClanOwnerId || 
          ControllerConstants.NOT_SET == oldClanTowerAttackerClanOwnerId) {
        //don't let the clan give up the tower since there's no attacker
        resBuilder.setStatus(ConcedeClanTowerWarStatus.NO_ATTACKER);
        return false;
      } else {
        //tower attacker wins
        aTower.setClanOwnerId(aTower.getClanAttackerId());
        aTower.setOwnedStartTime(curTime);
        aTower.setOwnerBattleWins(0);

        aTower.setClanAttackerId(ControllerConstants.NOT_SET);
        aTower.setAttackerBattleWins(0);
        aTower.setAttackStartTime(null);
        aTower.setLastRewardGiven(null);
        resBuilder.setStatus(ConcedeClanTowerWarStatus.SUCCESS);
        return true;
      }
    }

    resBuilder.setStatus(ConcedeClanTowerWarStatus.NOT_CLAN_LEADER);
    return false;
  }

  private void writeChangesToDB(ClanTower oldTower, ClanTower newTower, Timestamp curTime, User sender) {
    //write changes to clan_tower_history
    int oldOwnerId = oldTower.getClanOwnerId();
    int newOwnerId = newTower.getClanOwnerId();
    String reasonForEntry = "";
    int winnerId = newOwnerId;

    if(oldOwnerId == newOwnerId) { //attacker conceded
      reasonForEntry = Notification.ATTACKER_CONCEDED;
    }
    else { //owner conceded
      reasonForEntry = Notification.OWNER_CONCEDED;
    }
    List<ClanTower> tList = new ArrayList<ClanTower>();
    tList.add(oldTower);
    List<Integer> wList = new ArrayList<Integer>();
    wList.add(winnerId);
    UpdateUtils.get().updateTowerHistory(tList, reasonForEntry, wList);

    //write changes to clan_towers table
    if (!UpdateUtils.get().updateClanTowerOwnerAndOrAttacker(
        newTower.getId(), 
        newTower.getClanOwnerId(), newTower.getOwnedStartTime(), newTower.getOwnerBattleWins(),
        newTower.getClanAttackerId(), newTower.getAttackStartTime(), newTower.getAttackerBattleWins(),
        newTower.getLastRewardGiven(), newTower.getCurrentBattleId())) {
      log.error("problem with updating a clan tower during a ConcedeClanTowerWarRequest." +
          " old tower=" + oldTower +
          " new tower=" + newTower +
          " user=" + sender + 
          " time of request=" + curTime);
    }

  }

  private void sendTowersAndNotifications(int ownerBefore, int ownerAfter, ClanTower tower,
      Clan oldClanTowerAttacker, Clan oldClanTowerOwner) {
    Notification clanTowerWarNotification = new Notification ();
    List<ClanTower> changedTowers = new ArrayList<ClanTower>();
    changedTowers.add(tower);
    
    String losingClan = "";
    String winningClan = "";
    if (ownerBefore == ownerAfter) {
      MiscMethods.sendClanTowerProtosToClient(changedTowers, 
          server, ReasonForClanTowerChange.ATTACKER_CONCEDED);
      
      losingClan = oldClanTowerAttacker.getName();
      winningClan = oldClanTowerOwner.getName();
      
    }
    else  {
      MiscMethods.sendClanTowerProtosToClient(changedTowers, 
          server, ReasonForClanTowerChange.OWNER_CONCEDED);

      losingClan = oldClanTowerOwner.getName();
      winningClan = oldClanTowerAttacker.getName();
    }
    clanTowerWarNotification.setAsClanTowerWarClanConceded(
        losingClan, winningClan, tower.getTowerName());
    
    MiscMethods.writeGlobalNotification(clanTowerWarNotification, server);
  }
}
