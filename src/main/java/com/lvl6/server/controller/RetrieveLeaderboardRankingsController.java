package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Tuple;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveLeaderboardRankingsRequestEvent;
import com.lvl6.events.response.RetrieveLeaderboardRankingsResponseEvent;
import com.lvl6.info.User;
import com.lvl6.leaderboards.LeaderBoardUtilImpl;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRankingsRequestProto;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRankingsResponseProto;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRankingsResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveLeaderboardRankingsResponseProto.RetrieveLeaderboardStatus;
import com.lvl6.proto.InfoProto.LeaderboardType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component
@DependsOn("gameServer")
public class RetrieveLeaderboardRankingsController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() {
  }.getClass().getEnclosingClass());

  @Autowired
  public LeaderBoardUtilImpl leader;

  public RetrieveLeaderboardRankingsController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveLeaderboardRankingsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_LEADERBOARD_RANKINGS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveLeaderboardRankingsRequestProto reqProto = ((RetrieveLeaderboardRankingsRequestEvent) event)
        .getRetrieveLeaderboardRankingsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    int eventId = reqProto.getEventId();
    int afterThisRank = reqProto.getAfterThisRank();

    RetrieveLeaderboardRankingsResponseProto.Builder resBuilder = RetrieveLeaderboardRankingsResponseProto
        .newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setEventId(eventId);
    resBuilder.setAfterThisRank(afterThisRank);

    server.lockPlayer(senderProto.getUserId());
    try {
      LeaderboardType leaderboardType = LeaderboardType.EVENT;
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int userId = user.getId();
      boolean legitRetrieval = checkLegitRetrieval(resBuilder, user,	eventId);
      Map<Integer, UserRankScore> lurs = null;
      if (legitRetrieval) {
        int rank = (int) leader.getRankForEventAndUser(eventId, userId);
        double score = leader.getScoreForEventAndUser(eventId, userId);

        resBuilder.setRetriever(CreateInfoProtoUtils.createMinimumUserProtoWithLevelForLeaderboard(
            user, LeaderboardType.EVENT, rank, score));

        //TODO: FIX THIS IMPLEMENTATION
        lurs = getUsersAfterThisRank(eventId, afterThisRank);

        if (lurs != null) {
          List<User> resultUsers = new ArrayList<User>(RetrieveUtils.userRetrieveUtils().getUsersByIds(new ArrayList<Integer>(lurs.keySet())).values());
          log.info("Populating leaderboard results for event: "+eventId+" after this rank: "+afterThisRank+" found results: "+resultUsers.size());
          for (User u : resultUsers) {
            UserRankScore urs = lurs.get(u.getId());
            log.info("Rank: "+urs.rank+" User: "+urs.userId+" Score: "+urs.score);
            resBuilder.addResultPlayers(CreateInfoProtoUtils.createMinimumUserProtoWithLevelForLeaderboard(u, leaderboardType, urs.rank, urs.score));
            resBuilder.addFullUsers(CreateInfoProtoUtils.createFullUserProtoFromUser(u));
          }
        }
      }

      RetrieveLeaderboardRankingsResponseProto resProto = resBuilder.build();
      RetrieveLeaderboardRankingsResponseEvent resEvent = new RetrieveLeaderboardRankingsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveLeaderboardRankingsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error(
          "exception in RetrieveLeaderboardController processEvent",
          e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }

  }

  private Map<Integer, UserRankScore> getUsersAfterThisRank(int eventId,	int afterThisRank) {
    Set<Tuple> usrs = new HashSet<Tuple>();
    log.info("Retrieving event: "+eventId+" afterThisRank: "+afterThisRank);

    usrs = leader.getEventTopN(eventId, afterThisRank, afterThisRank+ControllerConstants.LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE);

    Map<Integer, UserRankScore> lurs = new LinkedHashMap<Integer, UserRankScore>();
    Iterator<Tuple> it = usrs.iterator();
    int counter = 1;
    while(it.hasNext()) {
      Tuple t = it.next();
      Integer userId = Integer.valueOf(t.getElement());
      UserRankScore urs = new UserRankScore(userId, t.getScore(), counter+afterThisRank);
      lurs.put(userId, urs);
      log.info(urs.toString());
      counter++;
    }
    return lurs;
  }

  private boolean checkLegitRetrieval(Builder resBuilder, User user,
      int eventId) {
    if (user == null || 0 >= eventId) {
      resBuilder.setStatus(RetrieveLeaderboardStatus.OTHER_FAIL);
      log.error("user is " + user + ", event id="
          + eventId);
      return false;
    }
    resBuilder.setStatus(RetrieveLeaderboardStatus.SUCCESS);
    return true;
  }

  public class UserRankScore{
    public UserRankScore(Integer userId, Double score, Integer rank) {
      super();
      this.userId = userId;
      this.score = score;
      this.rank = rank;
    }
    Integer userId;
    Double score;
    Integer rank;
    @Override
    public String toString() {
      return "UserRankScore [userId=" + userId + ", rank=" + rank + ", score=" + score + "]";
    }

  }



}