package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.LeaderboardEventReward;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class LeaderboardEventRewardRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<LeaderboardEventReward>> leaderboardEventIdsToLeaderboardEventRewards;

  private static final String TABLE_NAME = DBConstants.TABLE_LEADERBOARD_EVENT_REWARDS;

  public static Map<Integer, List<LeaderboardEventReward>> getleaderboardEventIdsToLeaderboardEventRewards() {
    log.debug("retrieving leaderboard event data");
    if (leaderboardEventIdsToLeaderboardEventRewards == null) {
      setStaticLeaderboardEventIdsToLeaderboardEventRewards();
    }
    return leaderboardEventIdsToLeaderboardEventRewards;
  }

  public static Map<Integer, List<LeaderboardEventReward>> getLeaderboardEventRewardsForIds(List<Integer> ids) {
    log.debug("retrieving LeaderboardEventRewards with ids " + ids);
    if (leaderboardEventIdsToLeaderboardEventRewards == null) {
      setStaticLeaderboardEventIdsToLeaderboardEventRewards();
    }
    Map<Integer, List<LeaderboardEventReward>> toReturn = new HashMap<Integer, List<LeaderboardEventReward>>();
    for (Integer id : ids) {
      toReturn.put(id,  leaderboardEventIdsToLeaderboardEventRewards.get(id));
    }
    return toReturn;
  }

  public static List<LeaderboardEventReward> getLeaderboardEventRewardsForId(int id) {
    log.debug("retrieving LeaderboardEventReward for id " + id);
    if (leaderboardEventIdsToLeaderboardEventRewards == null) {
      setStaticLeaderboardEventIdsToLeaderboardEventRewards();
    }
    return leaderboardEventIdsToLeaderboardEventRewards.get(id);
  }

  private static void setStaticLeaderboardEventIdsToLeaderboardEventRewards() {
    log.debug("setting static map of leader board event id to leader board reward");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, List<LeaderboardEventReward>> idsToLeaderboardEventRewardTemp = 
              new HashMap<Integer, List<LeaderboardEventReward>>();
          while(rs.next()) {  
            LeaderboardEventReward le = convertRSRowToLeaderboardEventReward(rs);
            
            if (le != null) {
              int leaderboardEventId = le.getLeaderboardEventId();
              List<LeaderboardEventReward> existingRewards = 
                  idsToLeaderboardEventRewardTemp.get(leaderboardEventId);
              
              if (null != existingRewards) {
                //map already has rewards pertaining to this event, so add to it
                existingRewards.add(le);
              } else {
                //le is a reward for a new event, create a new list for it
                List<LeaderboardEventReward> newEventRewards = new ArrayList<LeaderboardEventReward>();
                newEventRewards.add(le);
                
                idsToLeaderboardEventRewardTemp.put(leaderboardEventId, newEventRewards);  
              }
            }
          }
          leaderboardEventIdsToLeaderboardEventRewards = idsToLeaderboardEventRewardTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs,  null, conn);
  }

  public static void reload() {
    setStaticLeaderboardEventIdsToLeaderboardEventRewards();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static LeaderboardEventReward convertRSRowToLeaderboardEventReward(ResultSet rs) throws SQLException {
    int i = 1;
    int leaderboardEventId = rs.getInt(i++);
    int minRank = rs.getInt(i++);
    int maxRank = rs.getInt(i++);
    int goldRewarded = rs.getInt(i++);
    String backgroundImageName = rs.getString(i++);
    String prizeImageName = rs.getString(i++);
    int blue = rs.getInt(i++);
    int green = rs.getInt(i++);
    int red = rs.getInt(i++);
    
    return new LeaderboardEventReward(leaderboardEventId, minRank, maxRank, goldRewarded, 
        backgroundImageName, prizeImageName, blue, green, red);
  }
}
