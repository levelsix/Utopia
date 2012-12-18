package com.lvl6.scheduledtasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import redis.clients.jedis.Tuple;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.LeaderboardEvent;
import com.lvl6.info.LeaderboardEventReward;
import com.lvl6.info.User;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.misc.MiscMethods;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRewardRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class LeaderboardEventScheduledTasks {
	private static Logger log = LoggerFactory.getLogger(LeaderboardEventScheduledTasks.class);

	@Autowired
	protected HazelcastInstance hazel;

	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

	@Autowired
	protected GameServer server;

	protected void setServer(GameServer server) {
		this.server = server;
	}

	protected GameServer getServer() {
		return server;
	}

	@Autowired
	protected LeaderBoardUtil leader;

	protected void setLeader(LeaderBoardUtil leader) {
		this.leader = leader;
	}

	protected LeaderBoardUtil getLeader() {
		return leader;
	}

	@Scheduled(fixedRate = 10000)
	public void checkForEventsEnded() {
		ILock lock = hazel.getLock("EventsEndedScheduledTaskLock");
		boolean gotLock = false;
		try {
			if (lock.tryLock()) {
				gotLock = true;
				Collection<LeaderboardEvent> events = LeaderboardEventRetrieveUtils
						.getIdsToLeaderboardEvents().values();
				if (events == null)
					return;
				for (LeaderboardEvent event : events) {
					checkEndOfEvent(event);
				}
			}
		} catch (Exception e) {
			log.error("Error checking leaderboard events ended", e);
		} finally {
			if (gotLock)
				lock.forceUnlock();
		}
	}

	public void checkEndOfEvent(LeaderboardEvent event) {
		if (event.isRewardsGivenOut())
			return;
		if (event.getEndDate().getTime() > new Date().getTime())
			return;

		List<Integer> allUserIds = new ArrayList<Integer>();

		List<LeaderboardEventReward> rewards = LeaderboardEventRewardRetrieveUtils
				.getLeaderboardEventRewardsForId(event.getId());
		for (LeaderboardEventReward reward : rewards) {
			Set<Tuple> set = new HashSet<Tuple>();
			set = leader.getEventTopN(event.getId(), reward.getMinRank()-1, reward.getMaxRank()-1);

			List<Integer> userIds = new ArrayList<Integer>();
			Iterator<Tuple> it = set.iterator();
			while (it.hasNext()) {
				Tuple t = it.next();
				Integer userId = Integer.valueOf(t.getElement());

				// Make sure a score of at least 0
				userIds.add(userId);
			}

			log.info("Awarding " + reward.getGoldRewarded() + " gold for ranks "+reward.getMinRank()+"-"+reward.getMaxRank()+" to users "+userIds);
			if (!UpdateUtils.get().updateUsersAddDiamonds(userIds, reward.getGoldRewarded())) {
				log.error("Error updating user diamonds for userIds " + userIds + " and reward " + reward);
			}

			allUserIds.addAll(userIds);
		}

		Map<Integer, User> userIdsToUsers = RetrieveUtils.userRetrieveUtils().getUsersByIds(allUserIds);
		for (User user : userIdsToUsers.values()) {
			UpdateClientUserResponseEvent e = MiscMethods
					.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
			server.writeEvent(e);
		}

		if (!UpdateUtils.get().updateLeaderboardEventSetRewardGivenOut(event.getId())) {
			log.error("Error updating rewards given out for event " + event);
		} else {
		  event.setRewardsGivenOut(true);
		}
	}
}
