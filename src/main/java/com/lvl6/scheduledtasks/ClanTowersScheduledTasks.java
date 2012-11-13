package com.lvl6.scheduledtasks;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.lvl6.info.ClanTower;
import com.lvl6.misc.Notification;
import com.lvl6.properties.DBConstants;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;

public class ClanTowersScheduledTasks {
	private static Logger log = LoggerFactory.getLogger(ClanTowersScheduledTasks.class);
	
	private JdbcTemplate jdbcTemplate;

	@Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	@Resource
	protected HazelcastInstance hazel;
	
	protected long battle_length_milliseconds = 6*3600000;
	
	@Scheduled(fixedRate=300000)
	public void checkForBattlesEnded() {
		ILock battlesEndedLock = hazel.getLock("ClanTowersBattlesEndedScheduledTask");
		if(battlesEndedLock.tryLock()) {
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
				battlesEndedLock.unlock();
			}
		}		
	}
	
	protected void checkBattleForTower(ClanTower tower) {
		try {
			if(tower.getAttackStartTime() != null && tower.getAttackStartTime().getTime()+tower.getNumHoursForBattle() * 3600000 > System.currentTimeMillis()) {
				updateTowerHistory(tower);
				updateClanTower(tower);
			}
		}catch(Exception e) {
			log.error("Error checking battle ended", e);
		}
	}

	protected void updateClanTower(ClanTower tower) {
		if(tower.getAttackerBattleWins() > tower.getOwnerBattleWins()) {
			updateClanTowerAttackerWonBattle(tower);
			
			//sendGeneralNotification(tower, true);
		}else {
			updateClanTowerOwnerWonBattle(tower);
			
			//sendGeneralNotification(tower, false);
		}
	}
	/*
	protected void sendGeneralNotification(ClanTower tower, boolean attackerWon) {
		Notification clanTowerWarEnded = new Notification(server, allOnlinePlayers);
		
		clanTowerWarEnded.setNotificationAsClanTowerWarEnded(
				clanTowerOwnerName, clanTowerAttackerName, towerName, attackerWon);
	}*/
	
	protected void updateClanTowerOwnerWonBattle(ClanTower tower) {
		log.info("Updating clan tower {}. Owner won battle.");
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
	}

	protected void updateClanTowerAttackerWonBattle(ClanTower tower) {
		log.info("Updating clan tower {}. Attacker won battle.");
		jdbcTemplate.update("update "+DBConstants.TABLE_CLAN_TOWERS
			+" SET "
			+DBConstants.CLAN_TOWERS__CLAN_OWNER_ID
			+"="
			+tower.getClanAttackerId()
			+", "
			+DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID
			+"=NULL, "
			+", "
			+DBConstants.CLAN_TOWERS__ATTACK_START_TIME
			+"=NULL, "
			+DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS
			+"=0, "
			+DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID
			+"=NULL, "
			+DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN
			+"=NULL, "
			+DBConstants.CLAN_TOWERS__OWNED_START_TIME
			+"="
			+new Timestamp(System.currentTimeMillis())
			+", "
			+DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS
			+"=0 " +
			"WHERE "
			+DBConstants.CLAN_TOWERS__TOWER_ID
			+"="
			+tower.getId()						
			);
	}
	
	

	protected void updateTowerHistory(ClanTower tower) {
		jdbcTemplate.execute("insert into "+DBConstants.TABLE_CLAN_TOWERS_HISTORY
				+" ("
				+DBConstants.CLAN_TOWERS_HISTORY__OWNER_CLAN_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_CLAN_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__TOWER_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACK_START_TIME
				+DBConstants.CLAN_TOWERS_HISTORY__WINNER_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__OWNER_BATTLE_WINS+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_BATTLE_WINS+", "
				+DBConstants.CLAN_TOWERS_HISTORY__NUM_HOURS_FOR_BATTLE+", "
				+DBConstants.CLAN_TOWERS_HISTORY__LAST_REWARD_GIVEN+", "
				+DBConstants.CLAN_TOWERS_HISTORY__REASON_FOR_ENTRY+", "
				+") VALUES ("
				+tower.getClanOwnerId()+", "
				+tower.getClanAttackerId()+","
				+tower.getId()+", "
				+tower.getAttackStartTime()+", "
				+(tower.getAttackerBattleWins() > tower.getOwnerBattleWins() ? tower.getClanAttackerId() : tower.getClanOwnerId())+","
				+tower.getOwnerBattleWins()+", "
				+tower.getAttackerBattleWins()+", "
				+tower.getNumHoursForBattle()+", "
				+tower.getLastRewardGiven()+", "
				+Notification.BATTLE_ENDED+")"
		);
	}
	
	
	
	@Scheduled(fixedRate=300000)
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
			if(tower.getLastRewardGiven() == null || tower.getOwnedStartTime() == null) {
				return;
			}
			long currentTimeMillis = System.currentTimeMillis();
			int millisecondsToCollect = tower.getNumHoursToCollect()*3600000;
			if(tower.getLastRewardGiven().getTime() > tower.getOwnedStartTime().getTime()) {
				if(currentTimeMillis > tower.getLastRewardGiven().getTime() + millisecondsToCollect) {
					giveRewardsToClanMembers(tower);
					updateLastRewardTimeForClanTower(tower, currentTimeMillis);
				}
			}else if(currentTimeMillis > tower.getOwnedStartTime().getTime() + millisecondsToCollect) {
				giveRewardsToClanMembers(tower);
				updateLastRewardTimeForClanTower(tower, currentTimeMillis);
			}
		}catch(Exception e){
			log.error("Error distributing tower rewards", e);
		}
	}


	protected void updateLastRewardTimeForClanTower(ClanTower tower, long currentTimeMillis) {
		jdbcTemplate.update("update "+DBConstants.TABLE_CLAN_TOWERS
			+" SET "
			+DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN
			+"="
			+new Timestamp(currentTimeMillis)
			+" WHERE "+DBConstants.CLAN_TOWERS__TOWER_ID
			+"=?"
		, tower.getId());
	}


	protected void giveRewardsToClanMembers(ClanTower tower) {
		log.info("Distributing rewards for tower: {} to clan: {}", tower.getId(), tower.getClanOwnerId());
		jdbcTemplate.update("update "
			+DBConstants.TABLE_CLAN_TOWERS
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
