package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class UserQuestsCompletedDefeatTypeJobsRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS;
  
  public static List<Integer> getUserDefeatTypeJobsCompletedForQuest(int userId, int questId) {
    log.info("getting user defeatTypeJobs done for quest num " + questId);
    List<Integer> completedDefeatTypeJobs = new ArrayList<Integer>();
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID, questId);
    
    ResultSet rs = DBConnection.selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          completedDefeatTypeJobs.add(rs.getInt(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID));
        }
        return completedDefeatTypeJobs;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    } 
    return completedDefeatTypeJobs;
  }
}
