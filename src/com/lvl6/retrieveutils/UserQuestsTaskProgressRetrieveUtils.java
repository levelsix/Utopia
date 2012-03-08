package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*should make a UserQuestsTaskProgress*/
public class UserQuestsTaskProgressRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS;
  
  public static Map<Integer, Map<Integer, Integer>> getQuestIdToTaskIdsToNumTimesActedInQuest(int userId) {
    log.info("retrieving user taskProgress info for userId " + userId);

    return convertRSToQuestIdToTaskIdsToNumTimesActedForQuestMap(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
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
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return questIdToTaskIdsToNumTimesActedForQuest;
  }
  
}
