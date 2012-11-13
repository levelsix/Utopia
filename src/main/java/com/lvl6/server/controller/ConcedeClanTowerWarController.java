package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginClanTowerWarRequestEvent;
import com.lvl6.events.request.ConcedeClanTowerWarRequestEvent;
import com.lvl6.events.response.ConcedeClanTowerWarResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.BeginClanTowerWarStatus;
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

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private JdbcTemplate jdbcTemplate;

  @Resource
  public void setDataSource(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
  
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
    return new BeginClanTowerWarRequestEvent();
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
    
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      
      ClanTower aTower = ClanTowerRetrieveUtils.getClanTower(towerId);
      Clan clanTowerAttacker = ClanRetrieveUtils.getClanWithId(aTower.getClanAttackerId());
      Clan clanTowerOwner = ClanRetrieveUtils.getClanWithId(aTower.getClanOwnerId());
      
      boolean legit = checkLegitConcedeClanTowerWarRequest(
    		  resBuilder, user, clanTowerAttacker, clanTowerOwner, aTower, curTime);
      
      ConcedeClanTowerWarResponseEvent resEvent = new ConcedeClanTowerWarResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setConcedeClanTowerWarResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
    	writeChangesToDB(aTower, curTime, user);
    	
    	//sendGeneralNotification(ownerIdBefore, ownerIdAfter,
    	//	attackerIdBefore, attackerIdAfter, clan, aTower);
      }
    } catch (Exception e) {
      log.error("exception in BeginClanTowerWarController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  //aTower can be modified to store some new data, as in an owner, or attacker id
  //to be used as a second return value
  private boolean checkLegitConcedeClanTowerWarRequest(Builder resBuilder, User user, 
		  Clan clanTowerAttacker, Clan clanTowerOwner, ClanTower aTower, Timestamp curTime) {
    if (user == null) {
      resBuilder.setStatus(ConcedeClanTowerWarStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    //check if the tower is valid
    if (null == aTower || null == clanTowerOwner || null == clanTowerAttacker) {
    	//empty tower
    	resBuilder.setStatus(ConcedeClanTowerWarStatus.OTHER_FAIL);
    	log.error("tower requested is null, or clan tower owner or attacker is null. aTower=" +
    			aTower + " clanTowerOwner=" + clanTowerOwner + " clanTowerAttacker=" + clanTowerAttacker);
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
    if (clanTowerAttacker.getId() != clanOfUser &&
    	clanTowerOwner.getId() != clanOfUser) {
    	//user is not owner or attacker
    	resBuilder.setStatus(ConcedeClanTowerWarStatus.NOT_CLAN_TOWER_WAR_PARTICIPANT);
    	log.error("user is not a clan tower owner or attacker.");
    	return false;
    }
    
    int clanTowerOwnerClanOwnerId = clanTowerOwner.getOwnerId();
    int clanTowerAttackerClanOwnerId = clanTowerAttacker.getOwnerId();
    int userId = user.getId();
    
    //check if the request sender is the clan leader
    if(clanTowerAttackerClanOwnerId == userId) {
    	//tower owner wins
    	aTower.setClanAttackerId(ControllerConstants.NOT_SET);
    	aTower.setAttackerBattleWins(0);
    	aTower.setAttackStartTime(null);
    	resBuilder.setStatus(ConcedeClanTowerWarStatus.SUCCESS);
    	return true;
    }
    
    if(clanTowerOwnerClanOwnerId == userId) {
    	//tower attacker wins
    	aTower.setClanOwnerId(aTower.getClanAttackerId());
    	aTower.setOwnedStartTime(curTime);
    	aTower.setOwnerBattleWins(0);
    	
    	aTower.setClanAttackerId(ControllerConstants.NOT_SET);
    	aTower.setAttackStartTime(null);
    	aTower.setAttackerBattleWins(0);
    	resBuilder.setStatus(ConcedeClanTowerWarStatus.SUCCESS);
    	return true;
    }
    
    resBuilder.setStatus(ConcedeClanTowerWarStatus.NOT_CLAN_LEADER);
    return false;
  }

  private void writeChangesToDB(ClanTower aClanTower, Timestamp curTime, User sender) {
	  if (!UpdateUtils.get().updateClanTowerOwnerAndOrAttacker(
			  aClanTower.getId(), 
			  aClanTower.getClanOwnerId(), aClanTower.getOwnedStartTime(), aClanTower.getOwnerBattleWins(),
			  aClanTower.getClanAttackerId(), aClanTower.getAttackStartTime(), aClanTower.getAttackerBattleWins(),
			  aClanTower.getLastRewardGiven())) {
		  log.error("problem with updating a clan tower during a ConcedeClanTowerWarRequest." +
				  " clan tower=" + aClanTower +
				  " user=" + sender + 
				  " time of request=" + curTime);
	  }
	  //TODO: write changes to clan_tower_history
	  
  }
  
  private void sendGeneralNotification(
		  int ownerBefore, int ownerAfter, int attackerBefore, int attackerAfter,
		  Clan aClan, ClanTower aTower) {
	  Notification clanTowerWarNotification = new Notification (server, playersByPlayerId.values());
	  if (ownerBefore != ownerAfter) {
		  clanTowerWarNotification.setNotificationAsClanTowerStatus();
	  }
	  else if (attackerBefore != attackerAfter) {
		  //attackers should be different
		  
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
