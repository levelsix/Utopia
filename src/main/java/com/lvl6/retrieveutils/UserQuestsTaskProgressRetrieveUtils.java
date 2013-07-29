package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*should make a UserQuestsTaskProgress*/
@Component @DependsOn("gameServer") public class UserQuestsTaskProgressRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS;
  
  public static Map<Integer, Map<Integer, Integer>> getQuestIdToTaskIdsToNumTimesActedInQuest(int userId) {
    log.debug("retrieving user's quest id to (task progress map) map for user " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Map<Integer, Integer>> questIdToTaskIdsToNumTimesActedInQuest = convertRSToQuestIdToTaskIdsToNumTimesActedForQuestMap(rs);
    DBConnection.get().close(rs, null, conn);
    return questIdToTaskIdsToNumTimesActedInQuest;
  }
  
  private static Map<Integer, Map<Integer, Integer>> convertRSToQuestIdToTaskIdsToNumTimesActedForQuestMap(ResultSet rs) {
    Map<Integer, Map<Integer, Integer>> questIdToTaskIdsToNumTimesActedForQuest = new HashMap<Integer, Map<Integer, Integer>>();

    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          Integer questId = rs.getInt(DBConstants.USER_QUESTS_TASK_PROGRESS__QUEST_ID);
          int taskId = rs.getInt(DBConstants.USER_QUESTS_TASK_PROGRESS__TASK_ID);
          int numTimesActedForQuest = rs.getInt(DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED);

          if (questIdToTaskIdsToNumTimesActedForQuest.get(questId) == null) {
            questIdToTaskIdsToNumTimesActedForQuest.put(questId, new HashMap<Integer, Integer>());
          }
          questIdToTaskIdsToNumTimesActedForQuest.get(questId).put(taskId, numTimesActedForQuest);
        }
        return questIdToTaskIdsToNumTimesActedForQuest;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return questIdToTaskIdsToNumTimesActedForQuest;
  }
  
}
