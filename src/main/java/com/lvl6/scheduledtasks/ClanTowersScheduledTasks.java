package com.lvl6.scheduledtasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class ClanTowersScheduledTasks {
	private static Logger log = LoggerFactory.getLogger(ClanTowersScheduledTasks.class);

	private JdbcTemplate jdbcTemplate;

	@Resource
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// For sending messages to online people, NOTIFICATION FEATURE
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

	public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
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

	@Scheduled(fixedRate = 10000)
	public void checkForBattlesEnded() {
		// ILock battlesEndedLock =
		// hazel.getLock("ClanTowersBattlesEndedScheduledTask");
		// if(battlesEndedLock.tryLock()) {
		if (server.lockClanTowersTable()) {
			try {
				List<ClanTower> clanTowers = ClanTowerRetrieveUtils.getAllClanTowers();
				if (clanTowers == null)
					return;
				for (ClanTower tower : clanTowers) {
					checkBattleForTower(tower);
				}
			} catch (Exception e) {
				log.error("Error checking battles ended", e);
			} finally {
				server.unlockClanTowersTable();
				// battlesEndedLock.unlock();
			}
		}
	}

	protected void checkBattleForTower(ClanTower tower) {
		try {
			if (tower.getAttackStartTime() != null
					&& tower.getAttackStartTime().getTime() + tower.getNumHoursForBattle() * 3600000 < new Date()
							.getTime()) {
				updateTowerHistory(tower);
				updateClanTower(tower);
			}
		} catch (Exception e) {
			log.error("Error checking battle ended", e);
		}
	}

	protected void updateClanTower(ClanTower tower) {
		List<ClanTower> changedTowers = new ArrayList<ClanTower>();
		changedTowers.add(tower);

		if (tower.getAttackerBattleWins() > tower.getOwnerBattleWins()) {
			sendGeneralNotification(tower, true);
			updateClanTowerAttackerWonBattle(tower);

			MiscMethods.sendClanTowerProtosToClient(changedTowers, server,
					ReasonForClanTowerChange.ATTACKER_WON);
		} else {
			sendGeneralNotification(tower, false);
			updateClanTowerOwnerWonBattle(tower);

			MiscMethods
					.sendClanTowerProtosToClient(changedTowers, server, ReasonForClanTowerChange.OWNER_WON);
		}
	}

	protected void sendGeneralNotification(ClanTower tower, boolean attackerWon) {
		Notification clanTowerWarEnded = new Notification();
		String clanTowerOwnerName = ClanRetrieveUtils.getClanWithId(tower.getClanOwnerId()).getName();
		String clanTowerAttackerName = ClanRetrieveUtils.getClanWithId(tower.getClanAttackerId()).getName();
		String towerName = tower.getTowerName();

		clanTowerWarEnded.setAsClanTowerWarClanWon(clanTowerAttackerName, clanTowerOwnerName, towerName,
				attackerWon);

		MiscMethods.writeGlobalNotification(clanTowerWarEnded, server);
	}

	protected void updateClanTowerOwnerWonBattle(ClanTower tower) {
		log.info("Owner won battle. Updating clan tower " + tower + ".");
		jdbcTemplate.update("update " + DBConstants.TABLE_CLAN_TOWERS + " SET "
				+ DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID + "=NULL, "
				+ DBConstants.CLAN_TOWERS__ATTACK_START_TIME + "=NULL, "
				+ DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS + "=0, "
				+ DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS + "=0 " + "WHERE "
				+ DBConstants.CLAN_TOWERS__TOWER_ID + "=" + tower.getId());

		tower.setClanAttackerId(ControllerConstants.NOT_SET);
		tower.setAttackStartTime(null);
		tower.setAttackerBattleWins(0);
		tower.setOwnerBattleWins(0);
	}

	protected void updateClanTowerAttackerWonBattle(ClanTower tower) {
		log.info("Attacker won battle. Updating clan tower " + tower + ".");
		Timestamp t = new Timestamp(new Date().getTime());
		jdbcTemplate.update("update " + DBConstants.TABLE_CLAN_TOWERS + " SET "
				+ DBConstants.CLAN_TOWERS__CLAN_OWNER_ID + "=" + tower.getClanAttackerId() + ", "
				+ DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID + "=NULL, "
				+ DBConstants.CLAN_TOWERS__ATTACK_START_TIME + "=NULL, "
				+ DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS + "=0, "
				+ DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN + "=NULL, "
				+ DBConstants.CLAN_TOWERS__OWNED_START_TIME + "=?, "
				+ DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS + "=0 " + "WHERE "
				+ DBConstants.CLAN_TOWERS__TOWER_ID + "=" + tower.getId(), t);

		tower.setClanOwnerId(tower.getClanAttackerId());
		tower.setClanAttackerId(ControllerConstants.NOT_SET);
		tower.setAttackStartTime(null);
		tower.setAttackerBattleWins(0);
		tower.setOwnedStartTime(new Date(t.getTime()));
		tower.setOwnerBattleWins(0);
	}

	protected void updateTowerHistory(ClanTower tower) {
	  int winnerId = (tower.getAttackerBattleWins() > tower.getOwnerBattleWins() ? tower.getClanAttackerId()
        : tower.getClanOwnerId());
    List<ClanTower> tList = new ArrayList<ClanTower>();
    tList.add(tower);
    List<Integer> wList = new ArrayList<Integer>();
    wList.add(winnerId);
	  UpdateUtils.get().updateTowerHistory(tList, Notification.CLAN_TOWER_WAR_ENDED, wList);
//		String attStart = tower.getAttackStartTime() == null ? "null" : "\""
//				+ new Timestamp(tower.getAttackStartTime().getTime()) + "\"";
//		String lastReward = tower.getLastRewardGiven() == null ? "null" : "\""
//				+ new Timestamp(tower.getLastRewardGiven().getTime()) + "\"";
//		jdbcTemplate.execute("insert into "
//				+ DBConstants.TABLE_CLAN_TOWERS_HISTORY
//				+ " ("
//				+ DBConstants.CLAN_TOWERS_HISTORY__OWNER_CLAN_ID
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_CLAN_ID
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__TOWER_ID
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__ATTACK_START_TIME
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__WINNER_ID
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__OWNER_BATTLE_WINS
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_BATTLE_WINS
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__NUM_HOURS_FOR_BATTLE
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__LAST_REWARD_GIVEN
//				+ ", "
//				+ DBConstants.CLAN_TOWERS_HISTORY__REASON_FOR_ENTRY
//				+ ") VALUES ("
//				+ tower.getClanOwnerId()
//				+ ", "
//				+ tower.getClanAttackerId()
//				+ ","
//				+ tower.getId()
//				+ ", "
//				+ attStart
//				+ ", "
//				+  + "," + tower.getOwnerBattleWins() + ", "
//				+ tower.getAttackerBattleWins() + ", " + tower.getNumHoursForBattle() + ", " + lastReward
//				+ ", " + "\"" +  + "\")");
	}

	@Scheduled(fixedRate = 10000)
	public void distributeClanTowerRewards() {
		// ILock towerRewardsLock =
		// hazel.getLock("ClanTowersRewardsScheduledTask");
		// if(towerRewardsLock.tryLock()) {
		if (server.lockClanTowersTable()) {
			try {
				List<ClanTower> clanTowers = ClanTowerRetrieveUtils.getAllClanTowers();
				if (clanTowers == null)
					return;
				for (ClanTower tower : clanTowers) {
					distributeRewardsForTower(tower);
				}
			} finally {
				server.unlockClanTowersTable();
				// towerRewardsLock.unlock();
			}
		}
	}

	protected void distributeRewardsForTower(ClanTower tower) {
		try {
			if (tower.getClanOwnerId() == ControllerConstants.NOT_SET) {
				return;
			}

			if (tower.getLastRewardGiven() == null) {
				if (tower.getOwnedStartTime() != null) {
					tower.setLastRewardGiven(tower.getOwnedStartTime());
				} else {
					return;
				}
			}

			long currentTimeMillis = new Date().getTime();
			int millisecondsToCollect = tower.getNumHoursToCollect() * 3600000;

			if (currentTimeMillis > tower.getLastRewardGiven().getTime() + millisecondsToCollect) {
				giveRewardsToClanMembers(tower);
				insertIntoUserCurrencyHistory(tower, currentTimeMillis);
				updateLastRewardTimeForClanTower(tower, currentTimeMillis);
				sendNotificationForRewardDistribution(tower, tower.getClanOwnerId());
				sendUpdateUserMessagesToClanMembers(tower.getClanOwnerId());
			}
		} catch (Exception e) {
			log.error("Error distributing tower rewards", e);
		}
	}

	protected void updateLastRewardTimeForClanTower(ClanTower tower, long currentTimeMillis) {
		jdbcTemplate.update("update " + DBConstants.TABLE_CLAN_TOWERS + " SET "
				+ DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN + "=" + "?" + " WHERE "
				+ DBConstants.CLAN_TOWERS__TOWER_ID + "=?", new Timestamp(tower.getLastRewardGiven()
				.getTime() + tower.getNumHoursToCollect() * 3600000), tower.getId());
	}

	protected void giveRewardsToClanMembers(ClanTower tower) {
		log.info("Distributing rewards for tower: {} to clan: {}", tower.getId(), tower.getClanOwnerId());
		jdbcTemplate.update(
				"update " + DBConstants.TABLE_USER + " SET " + DBConstants.USER__COINS + "=("
						+ DBConstants.USER__COINS + " + ?), " + DBConstants.USER__DIAMONDS + "=("
						+ DBConstants.USER__DIAMONDS + " + ?) WHERE " + DBConstants.USER__CLAN_ID + "="
						+ tower.getClanOwnerId(), tower.getSilverReward(), tower.getGoldReward());
	}

	protected void insertIntoUserCurrencyHistory(ClanTower tower, long now) {
		// try catch not necessary, but just precaution
		try {
			List<User> users = RetrieveUtils.userRetrieveUtils().getUsersByClanId(tower.getClanOwnerId());
			int amount = users.size();
			int gold = tower.getGoldReward();
			List<Integer> userIds = getUserIds(users);
			List<Timestamp> dates = new ArrayList<Timestamp>(Collections.nCopies(amount, new Timestamp(now)));
			List<Integer> areSilver = new ArrayList<Integer>(Collections.nCopies(amount, 0));
			List<Integer> currenciesChange = new ArrayList<Integer>(Collections.nCopies(amount, gold));
			List<Integer> currenciesBefore = getCurrenciesBefore(users, gold);
			List<String> reasonsForChanges = new ArrayList<String>(Collections.nCopies(amount,
					ControllerConstants.UCHRFC__CLAN_TOWER_WAR_ENDED));
			int numInserted = InsertUtils.get().insertIntoUserCurrencyHistoryMultipleRows(userIds, dates,
					areSilver, currenciesChange, currenciesBefore, reasonsForChanges);
			log.info("Should be " + userIds.size() + ". Rows inserted into user_currency_history: "
					+ numInserted);
		} catch (Exception e) {
			log.error("Maybe table's not there or duplicate keys? ", e);
		}
	}

	private List<Integer> getCurrenciesBefore(List<User> users, int goldRewarded) {
		List<Integer> returnVal = new ArrayList<Integer>();
		for (User u : users) {
			returnVal.add(u.getDiamonds() - goldRewarded); // the gold was
															// rewarded to them
															// before writing to
															// history
		}
		return returnVal;
	}

	private List<Integer> getUserIds(List<User> users) {
		List<Integer> userIds = new ArrayList<Integer>();
		for (User u : users) {
			userIds.add(u.getId());
		}
		return userIds;
	}

	protected void sendUpdateUserMessagesToClanMembers(int clanId) {
		List<User> users = RetrieveUtils.userRetrieveUtils().getUsersByClanId(clanId);
		for (User user : users) {
			UpdateClientUserResponseEvent e = MiscMethods
					.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
			server.writeEvent(e);
		}
	}

	public void sendNotificationForRewardDistribution(ClanTower aTower, int clanId) {
		Notification n = new Notification();
		String towerName = aTower.getTowerName();
		int silverReward = aTower.getSilverReward();
		int goldReward = aTower.getGoldReward();
		int numHours = aTower.getNumHoursToCollect();

		n.setAsClanTowerWarDistributeRewards(towerName, silverReward, goldReward, numHours);

		GeneralNotificationResponseProto.Builder notificationProto = n.generateNotificationBuilder();

		GeneralNotificationResponseEvent aNotification = new GeneralNotificationResponseEvent(0);
		aNotification.setGeneralNotificationResponseProto(notificationProto.build());
		server.writeClanEvent(aNotification, clanId);
	}

	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

}
