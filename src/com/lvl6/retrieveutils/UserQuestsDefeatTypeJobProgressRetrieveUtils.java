package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserQuestsDefeatTypeJobProgress needed because you can just return a map- only two non-user fields*/
public class UserQuestsDefeatTypeJobProgressRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS;
  
  public static Map<Integer, Integer> getDefeatTypeJobIdsToNumDefeatedForUserQuest(int userId, int questId) {
    log.info("retrieving user defeatTypeJobProgress info for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID, questId);

    return convertRSToDefeatTypeJobIdsToNumTimesCompletedMap(DBConnection.selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME));
  }
  
  private static Map<Integer, Integer> convertRSToDefeatTypeJobIdsToNumTimesCompletedMap(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Integer> defeatTypeJobIdsToNumDefeated = new HashMap<Integer, Integer>();
        while(rs.next()) {
          int defeatTypeJobId = rs.getInt(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__DEFEAT_TYPE_JOB_ID);
          int numDefeated = rs.getInt(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED);
          defeatTypeJobIdsToNumDefeated.put(defeatTypeJobId, numDefeated);
        }
        return defeatTypeJobIdsToNumDefeated;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
}
