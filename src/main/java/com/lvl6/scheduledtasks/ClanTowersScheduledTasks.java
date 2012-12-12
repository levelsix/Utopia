package com.lvl6.scheduledtasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.lvl6.info.ClanTower;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;

public class ClanTowersScheduledTasks {
  private static Logger log = LoggerFactory.getLogger(ClanTowersScheduledTasks.class);

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

  @Autowired
  protected GameServer server;
  protected void setServer(GameServer server) {
    this.server = server;
  }
  protected GameServer getServer() {
    return server;
  }


  @Resource
  protected HazelcastInstance hazel;

  @Scheduled(fixedRate=10000)
  public void checkForBattlesEnded() {
    //ILock battlesEndedLock = hazel.getLock("ClanTowersBattlesEndedScheduledTask");
    //if(battlesEndedLock.tryLock()) {
    if(server.lockClanTowersTable()) {
      try {
        List<ClanTower> clanTowers = ClanTowerRetrieveUtils.getAllClanTowers();
        if(clanTowers == null) return;
        for(ClanTower tower : clanTowers) {
          checkBattleForTower(tower);
        }

      }catch(Exception e){
        log.error("Error checking battles ended", e);
      }
      finally {
        server.unlockClanTowersTable();
        //battlesEndedLock.unlock();
      }
    }		
  }

  protected void checkBattleForTower(ClanTower tower) {
    try {
      if(tower.getAttackStartTime() != null && tower.getAttackStartTime().getTime()+tower.getNumHoursForBattle() * 3600000 < new Date().getTime()) {
        updateTowerHistory(tower);
        updateClanTower(tower);
      }
    }catch(Exception e) {
      log.error("Error checking battle ended", e);
    }
  }

  protected void updateClanTower(ClanTower tower) {
    List<ClanTower> changedTowers = new ArrayList<ClanTower>();
    changedTowers.add(tower);

    if(tower.getAttackerBattleWins() > tower.getOwnerBattleWins()) {
      sendGeneralNotification(tower, true);
      updateClanTowerAttackerWonBattle(tower);

      MiscMethods.sendClanTowerProtosToClient(changedTowers, server, 
          ReasonForClanTowerChange.ATTACKER_WON);
    }else {
      sendGeneralNotification(tower, false);
      updateClanTowerOwnerWonBattle(tower);

      MiscMethods.sendClanTowerProtosToClient(changedTowers, server, 
          ReasonForClanTowerChange.OWNER_WON);
    }
  }

  protected void sendGeneralNotification(ClanTower tower, boolean attackerWon) {
    Notification clanTowerWarEnded = new Notification(server, playersByPlayerId.values());
    String clanTowerOwnerName = ClanRetrieveUtils.getClanWithId(tower.getClanOwnerId()).getName();
    String clanTowerAttackerName = ClanRetrieveUtils.getClanWithId(tower.getClanAttackerId()).getName();
    String towerName = tower.getTowerName();

    clanTowerWarEnded.setAsClanTowerWarClanWon(
        clanTowerAttackerName, clanTowerOwnerName, towerName, attackerWon);
    executor.execute(clanTowerWarEnded);
  }

  protected void updateClanTowerOwnerWonBattle(ClanTower tower) {
    log.info("Owner won battle. Updating clan tower "+tower+".");
    jdbcTemplate.update("update "+DBConstants.TABLE_CLAN_TOWERS
        +" SET "
        +DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID
        +"=NULL, "
        +DBConstants.CLAN_TOWERS__ATTACK_START_TIME
        +"=NULL, "
        +DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS
        +"=0, "
        +DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS
        +"=0 " +
        "WHERE "
        +DBConstants.CLAN_TOWERS__TOWER_ID
        +"="
        +tower.getId()						
        );

    tower.setClanAttackerId(ControllerConstants.NOT_SET);
    tower.setAttackStartTime(null);
    tower.setAttackerBattleWins(0);
    tower.setOwnerBattleWins(0);
  }

  protected void updateClanTowerAttackerWonBattle(ClanTower tower) {
    log.info("Attacker won battle. Updating clan tower "+tower+".");
    Timestamp t = new Timestamp(new Date().getTime());
    jdbcTemplate.update("update "+DBConstants.TABLE_CLAN_TOWERS
        +" SET "
        +DBConstants.CLAN_TOWERS__CLAN_OWNER_ID
        +"="
        +tower.getClanAttackerId()
        +", "
        +DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID
        +"=NULL, "
        +DBConstants.CLAN_TOWERS__ATTACK_START_TIME
        +"=NULL, "
        +DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS
        +"=0, "
        +DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN
        +"=NULL, "
        +DBConstants.CLAN_TOWERS__OWNED_START_TIME
        +"=?, "
        +DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS
        +"=0 " +
        "WHERE "
        +DBConstants.CLAN_TOWERS__TOWER_ID
        +"="
        +tower.getId()						
        , t);

    tower.setClanOwnerId(tower.getClanAttackerId());
    tower.setClanAttackerId(ControllerConstants.NOT_SET);
    tower.setAttackStartTime(null);
    tower.setAttackerBattleWins(0);
    tower.setOwnedStartTime(new Date(t.getTime()));
    tower.setOwnerBattleWins(0);
  }



  protected void updateTowerHistory(ClanTower tower) {
    String attStart = tower.getAttackStartTime() == null ? "null" : "\""+new Timestamp(tower.getAttackStartTime().getTime())+"\"";
    String lastReward = tower.getLastRewardGiven() == null ? "null" : "\""+new Timestamp(tower.getLastRewardGiven().getTime())+"\"";
    jdbcTemplate.execute("insert into "+DBConstants.TABLE_CLAN_TOWERS_HISTORY
        +" ("
        +DBConstants.CLAN_TOWERS_HISTORY__OWNER_CLAN_ID+", "
        +DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_CLAN_ID+", "
        +DBConstants.CLAN_TOWERS_HISTORY__TOWER_ID+", "
        +DBConstants.CLAN_TOWERS_HISTORY__ATTACK_START_TIME+", "
        +DBConstants.CLAN_TOWERS_HISTORY__WINNER_ID+", "
        +DBConstants.CLAN_TOWERS_HISTORY__OWNER_BATTLE_WINS+", "
        +DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_BATTLE_WINS+", "
        +DBConstants.CLAN_TOWERS_HISTORY__NUM_HOURS_FOR_BATTLE+", "
        +DBConstants.CLAN_TOWERS_HISTORY__LAST_REWARD_GIVEN+", "
        +DBConstants.CLAN_TOWERS_HISTORY__REASON_FOR_ENTRY
        +") VALUES ("
        +tower.getClanOwnerId()+", "
        +tower.getClanAttackerId()+","
        +tower.getId()+", "
        +attStart+", "
        +(tower.getAttackerBattleWins() > tower.getOwnerBattleWins() ? tower.getClanAttackerId() : tower.getClanOwnerId())+","
        +tower.getOwnerBattleWins()+", "
        +tower.getAttackerBattleWins()+", "
        +tower.getNumHoursForBattle()+", "
        +lastReward+", "
        +"\""+Notification.CLAN_TOWER_WAR_ENDED+"\")"
        );
  }



  @Scheduled(fixedRate=10000)
  public void distributeClanTowerRewards() {
    ILock towerRewardsLock = hazel.getLock("ClanTowersRewardsScheduledTask");
    if(towerRewardsLock.tryLock()) {
      try {
        List<ClanTower> clanTowers = ClanTowerRetrieveUtils.getAllClanTowers();
        if(clanTowers == null) return;
        for(ClanTower tower : clanTowers) {
          distributeRewardsForTower(tower);
        }
      }finally {
        towerRewardsLock.unlock();
      }
    }
  }

  protected void distributeRewardsForTower(ClanTower tower) {
    try {
      if (tower.getLastRewardGiven() == null) {
        if (tower.getOwnedStartTime() != null) {
          tower.setLastRewardGiven(tower.getOwnedStartTime());
        } else {
          return;
        }
      }

      long currentTimeMillis = new Date().getTime();
      int millisecondsToCollect = tower.getNumHoursToCollect()*3600000;

      if(currentTimeMillis > tower.getLastRewardGiven().getTime() + millisecondsToCollect) {
        giveRewardsToClanMembers(tower);
        updateLastRewardTimeForClanTower(tower, currentTimeMillis);
      }
    } catch(Exception e) {
      log.error("Error distributing tower rewards", e);
    }
  }


  protected void updateLastRewardTimeForClanTower(ClanTower tower, long currentTimeMillis) {
    jdbcTemplate.update("update "+DBConstants.TABLE_CLAN_TOWERS
        +" SET "
        +DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN
        +"="
        +"?"
        +" WHERE "+DBConstants.CLAN_TOWERS__TOWER_ID
        +"=?"
        , new Timestamp(tower.getLastRewardGiven().getTime()+tower.getNumHoursToCollect()*3600000)
        , tower.getId());
  }


  protected void giveRewardsToClanMembers(ClanTower tower) {
    log.info("Distributing rewards for tower: {} to clan: {}", tower.getId(), tower.getClanOwnerId());
    jdbcTemplate.update("update "
        +DBConstants.TABLE_USER
        +" SET "
        +DBConstants.USER__COINS
        +"=("
        +DBConstants.USER__COINS
        +" + ?), "
        +DBConstants.USER__DIAMONDS
        +"=("
        +DBConstants.USER__DIAMONDS
        +" + ?) WHERE "
        +DBConstants.USER__CLAN_ID
        +"="+tower.getClanOwnerId()
        ,tower.getSilverReward()
        ,tower.getGoldReward());
  }

  public HazelcastInstance getHazel() {
    return hazel;
  }

  public void setHazel(HazelcastInstance hazel) {
    this.hazel = hazel;
  }



}
