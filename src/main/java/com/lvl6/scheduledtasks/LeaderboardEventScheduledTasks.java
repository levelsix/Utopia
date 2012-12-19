package com.lvl6.scheduledtasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import redis.clients.jedis.Tuple;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.LeaderboardEvent;
import com.lvl6.info.LeaderboardEventReward;
import com.lvl6.info.User;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRewardRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;
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
	
	@Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;

	public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
    return playersByPlayerId;
  }

  public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
    this.playersByPlayerId = playersByPlayerId;
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
		
		//SEND NOTIFICATION FOR END OF TOURNAMENT (LEADERBOARD EVENT)
		notificationStuff(event, rewards, userIdsToUsers);
	}
	
	private void notificationStuff(LeaderboardEvent event, List<LeaderboardEventReward> rewards,
	    Map<Integer, User> userIdsToUsers) {
	  int eventId = event.getId();
	  
	  LeaderboardEventReward r = getFirstPlaceReward(rewards);
    if (null == r) {
      log.error("first place leader board event reward does not exist.");
      return;
    } else {
      sendGlobalNotification(eventId, r, userIdsToUsers);
    }
   
    sendIndividualNotifications(eventId, rewards, userIdsToUsers);
    
	}
	
	private void sendGlobalNotification(int eventId, LeaderboardEventReward r,
      Map<Integer, User> userIdsToUsers) {
	  
	  Set<Tuple> set = new HashSet<Tuple>();
    
    set = leader.getEventTopN(eventId, 0, 0); //the top player
    
    Iterator<Tuple> it = set.iterator();
    while (it.hasNext()) { //loop should only go once
      Tuple t = it.next();
      Integer userId = Integer.valueOf(t.getElement());
      
      if(userIdsToUsers.containsKey(userId)) {//user should be in there
        User u = userIdsToUsers.get(userId);
        GeneralNotificationResponseEvent resEvent =
            generateGlobalNotificationResponseEvent(u, r); 
        server.writeGlobalEvent(resEvent);
        
      } else {
        log.error("first place winner does not exist for leaderboard/tournament event");
        return;
      }
      
    }
	}
	
	private LeaderboardEventReward getFirstPlaceReward(List<LeaderboardEventReward> rList) {
	  for(LeaderboardEventReward r : rList) {
	    if(1 >= r.getMinRank() && 1 <= r.getMaxRank()) {
	      return r;
	    }
	  }
	  return null;
	}
	
	private GeneralNotificationResponseEvent generateGlobalNotificationResponseEvent(
	    User firstPlaceWinner, LeaderboardEventReward reward) {
	  String firstPlaceWinnerName = firstPlaceWinner.getName();
	  int gold = reward.getGoldRewarded();
	  
	  Notification global = new Notification();
    global.setAsLeaderboardEventEndedGlobal(firstPlaceWinnerName, gold);
    
    GeneralNotificationResponseProto.Builder b = global.generateNotificationBuilder();
    
    GeneralNotificationResponseEvent resEvent = 
        new GeneralNotificationResponseEvent(0); //0 just because.
    resEvent.setGeneralNotificationResponseProto(b.build());
    
    return resEvent;
	}
	
	private void sendIndividualNotifications(int eventId, 
	    List<LeaderboardEventReward> rList, Map<Integer, User> userIdsToUsers) {
	  for (LeaderboardEventReward reward : rList) {
      Set<Tuple> set = new HashSet<Tuple>();
      int gold = reward.getGoldRewarded();
      int minRank = reward.getMinRank();
      int maxRank = reward.getMaxRank();
      set = leader.getEventTopN(eventId, minRank-1, maxRank-1);

      Iterator<Tuple> it = set.iterator();
      while (it.hasNext()) {
        Tuple t = it.next();
        
        //notification message different for online and offline
        Integer userId = Integer.valueOf(t.getElement());
        boolean playerOnline = playersByPlayerId.containsKey(userId);
        
        Notification n = new Notification();
        n.setAsLeaderboardEventEndedIndividual(playerOnline, maxRank, gold);
        
        GeneralNotificationResponseProto.Builder b = n.generateNotificationBuilder();
        
        GeneralNotificationResponseEvent resEvent = 
            new GeneralNotificationResponseEvent(userId);
        resEvent.setGeneralNotificationResponseProto(b.build());
        
        if(playerOnline) {
          server.writeEvent(resEvent);
        } else {
          server.writeAPNSNotificationOrEvent(resEvent);
        }
      }
	  }
	}
}
