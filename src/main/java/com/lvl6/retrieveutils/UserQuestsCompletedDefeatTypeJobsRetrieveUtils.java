package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserQuestsCompletedDefeatTypeJobsRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS;

  @Cacheable(value="questIdToUserDefeatTypeJobsCompletedForQuestForUserCache", key="#userId")
  public Map<Integer, List<Integer>> getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(int userId) {
    log.debug("retrieving user's quest id to completed defeat type jobs map for user " + userId);
    Map <Integer, List<Integer>> questIdToUserDefeatTypeJobsCompleted = new HashMap<Integer, List<Integer>>();

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            Integer questId = rs.getInt(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID);
            Integer completedDefeatTypeJobId = rs.getInt(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID);
            if (questIdToUserDefeatTypeJobsCompleted.get(questId) == null) {
              questIdToUserDefeatTypeJobsCompleted.put(questId, new ArrayList<Integer>());
            }
            questIdToUserDefeatTypeJobsCompleted.get(questId).add(completedDefeatTypeJobId);
          }
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      } 
    }
    DBConnection.get().close(rs, null, conn);
    
    return questIdToUserDefeatTypeJobsCompleted;
  }

}
