package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*should make a UserQuestsDefeatTypeJobProgress*/
@Component @DependsOn("gameServer") public class UserQuestsDefeatTypeJobProgressRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS;
  
  public static Map<Integer, Map<Integer, Integer>> getQuestIdToDefeatTypeJobIdsToNumDefeated(int userId) {
    log.debug("retrieving user's quest id to (defeat type job progress map) map for user " + userId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = convertRSToQuestIdToDefeatTypeJobIdsToNumDefeatedMap(rs);
    DBConnection.get().close(rs, null, conn);
    return questIdToDefeatTypeJobIdsToNumDefeated;
  }
  
  private static Map<Integer, Map<Integer, Integer>> convertRSToQuestIdToDefeatTypeJobIdsToNumDefeatedMap(ResultSet rs) {
    Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = new HashMap<Integer, Map<Integer, Integer>>();

    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          Integer questId = rs.getInt(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID);
          int defeatTypeJobId = rs.getInt(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__DEFEAT_TYPE_JOB_ID);
          int numDefeated = rs.getInt(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED);

          if (questIdToDefeatTypeJobIdsToNumDefeated.get(questId) == null) {
            questIdToDefeatTypeJobIdsToNumDefeated.put(questId, new HashMap<Integer, Integer>());
          }
          questIdToDefeatTypeJobIdsToNumDefeated.get(questId).put(defeatTypeJobId, numDefeated);
        }
        return questIdToDefeatTypeJobIdsToNumDefeated;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return questIdToDefeatTypeJobIdsToNumDefeated;
  }
  
}
