package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveLeaderboardRequestEvent;
import com.lvl6.events.response.RetrieveLeaderboardResponseEvent;
import com.lvl6.info.User;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRequestProto;
import com.lvl6.proto.EventProto.RetrieveLeaderboardResponseProto;
import com.lvl6.proto.EventProto.RetrieveLeaderboardResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveLeaderboardResponseProto.RetrieveLeaderboardStatus;
import com.lvl6.proto.InfoProto.LeaderboardType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component
@DependsOn("gameServer")
public class RetrieveLeaderboardController extends EventController {

	private static Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	@Autowired
	public LeaderBoardUtil leader;

	public RetrieveLeaderboardController() {
		numAllocatedThreads = 5;
	}

	@Override
	public RequestEvent createRequestEvent() {
		return new RetrieveLeaderboardRequestEvent();
	}

	@Override
	public EventProtocolRequest getEventType() {
		return EventProtocolRequest.C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
	}

	@Override
	protected void processRequestEvent(RequestEvent event) throws Exception {
		RetrieveLeaderboardRequestProto reqProto = ((RetrieveLeaderboardRequestEvent) event)
				.getRetrieveLeaderboardRequestProto();

		MinimumUserProto senderProto = reqProto.getSender();

		LeaderboardType leaderboardType = reqProto.getLeaderboardType();
		int afterThisRank = reqProto.getAfterThisRank();

		RetrieveLeaderboardResponseProto.Builder resBuilder = RetrieveLeaderboardResponseProto
				.newBuilder();
		resBuilder.setSender(senderProto);
		resBuilder.setLeaderboardType(leaderboardType);
		resBuilder.setAfterThisRank(afterThisRank);

		server.lockPlayer(senderProto.getUserId());
		try {
			User user = RetrieveUtils.userRetrieveUtils().getUserById(
					senderProto.getUserId());

			boolean legitRetrieval = checkLegitRetrieval(resBuilder, user,
					leaderboardType);

			List<User> resultList = null;

			if (legitRetrieval) {
				int rank = getUserRankForLeaderboardType(user, leaderboardType);
				resBuilder.setRetrieverRank(rank);
				// TODO: populate resultList based on leaderboard type
				resultList = getUsersAroundThisRank(leaderboardType, afterThisRank);
				if (resultList != null) {
					for (User u : resultList) {
						resBuilder.addResultPlayers(CreateInfoProtoUtils
								.createFullUserProtoFromUser(u));
					}
				}
			}

			RetrieveLeaderboardResponseProto resProto = resBuilder.build();
			RetrieveLeaderboardResponseEvent resEvent = new RetrieveLeaderboardResponseEvent(senderProto.getUserId());
			resEvent.setTag(event.getTag());
			resEvent.setRetrieveLeaderboardResponseProto(resProto);

			server.writeEvent(resEvent);
		} catch (Exception e) {
			log.error(
					"exception in RetrieveLeaderboardController processEvent",
					e);
		} finally {
			server.unlockPlayer(senderProto.getUserId());
		}

	}

	private List<User> getUsersAroundThisRank(LeaderboardType leaderboardType,	int afterThisRank) {
		List<Integer> usrs = new ArrayList<Integer>();
		if (leaderboardType.equals(LeaderboardType.BEST_KDR)) {
			usrs = leader.getBattlesWonOverTotalBattlesRatioTopN(afterThisRank, afterThisRank+ControllerConstants.LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE);
		}
		if (leaderboardType.equals(LeaderboardType.MOST_BATTLES_WON)) {
			usrs = leader.getBattlesWonTopN(afterThisRank, afterThisRank+ControllerConstants.LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE);
		}
		if (leaderboardType.equals(LeaderboardType.MOST_EXP)) {
			usrs = leader.getTasksCompletedTopN(afterThisRank, afterThisRank+ControllerConstants.LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE);
		}
		if (leaderboardType.equals(LeaderboardType.MOST_SILVER)) {
			usrs = leader.getSilverForTopN(afterThisRank, afterThisRank+ControllerConstants.LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE);
		}
		return new ArrayList<User>(RetrieveUtils.userRetrieveUtils().getUsersByIds(usrs).values());
	}
	
	

	private int getUserRankForLeaderboardType(User user,
			LeaderboardType leaderboardType) {
		if (leaderboardType == LeaderboardType.BEST_KDR
				&& user.getBattlesWon() + user.getBattlesLost() < ControllerConstants.LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION)
			return 0;
		if (leaderboardType.equals(LeaderboardType.BEST_KDR)) {
			return ((Double) leader
					.getBattlesWonOverTotalBattlesRatioRankForUser(user.getId()))
					.intValue();
		}
		if (leaderboardType.equals(LeaderboardType.MOST_BATTLES_WON)) {
			return ((Double) leader.getBattlesWonRankForUser(user.getId()))
					.intValue();
		}
		if (leaderboardType.equals(LeaderboardType.MOST_EXP)) {
			return ((Double) leader.getTasksCompletedRankForUser(user.getId()))
					.intValue();
		}
		if (leaderboardType.equals(LeaderboardType.MOST_SILVER)) {
			return ((Double) leader.getSilverForRankUser(user.getId()))
					.intValue();
		}
		return 0;
	}

	private boolean checkLegitRetrieval(Builder resBuilder, User user,
			LeaderboardType leaderboardType) {
		if (user == null || leaderboardType == null) {
			resBuilder.setStatus(RetrieveLeaderboardStatus.OTHER_FAIL);
			log.error("user is " + user + ", leaderboard type="
					+ leaderboardType);
			return false;
		}
		resBuilder.setStatus(RetrieveLeaderboardStatus.SUCCESS);
		return true;
	}

}