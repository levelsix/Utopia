package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.UserQuest;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class UserQuestRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS;
  
  public static List<UserQuest> getInProgressUserQuestsForUser(int userId) {
    log.info("retrieving user quests for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    return convertRSToUserQuests(DBConnection.selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME));
  }

  public static List<UserQuest> getUserQuestsForUser(int userId) {
    log.info("retrieving user quests for userId " + userId);
    return convertRSToUserQuests(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }
  
  private static List<UserQuest> convertRSToUserQuests(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserQuest> userQuests = new ArrayList<UserQuest>();
        while(rs.next()) {
          userQuests.add(convertRSRowToUserQuest(rs));
        }
        return userQuests;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static UserQuest convertRSRowToUserQuest(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int questId = rs.getInt(i++);
    boolean isRedeemed = rs.getBoolean(i++);
    boolean tasksComplete = rs.getBoolean(i++);
    boolean defeatTypeJobsComplete = rs.getBoolean(i++);
    boolean marketplaceJobsComplete = rs.getBoolean(i++);

    UserQuest userQuest = new UserQuest(userId, questId, isRedeemed, tasksComplete, 
        defeatTypeJobsComplete, marketplaceJobsComplete);
    return userQuest;
  }
  
}
