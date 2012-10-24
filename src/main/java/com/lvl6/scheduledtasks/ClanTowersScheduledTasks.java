package com.lvl6.scheduledtasks;

import java.sql.Timestamp;
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
