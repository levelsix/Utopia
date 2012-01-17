package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
public class UserTaskRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_TASKS;
  
  public static Map<Integer, Integer> getTaskIdToNumTimesActedInRankForUser(int userId) {
    log.info("retrieving user task info for userId " + userId);
    return convertRSToTaskIdToNumTimesCompletedMap(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }
  
  private static Map<Integer, Integer> convertRSToTaskIdToNumTimesCompletedMap(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Integer> taskIdToNumTimesCompletedInRank = new HashMap<Integer, Integer>();
        while(rs.next()) {  //should only be one
          int taskId = rs.getInt(DBConstants.USER_TASK__TASK_ID);
          int numTimesCompletedInRank = rs.getInt(DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK);
          taskIdToNumTimesCompletedInRank.put(taskId, numTimesCompletedInRank);
        }
        return taskIdToNumTimesCompletedInRank;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }
    return null;
  }
  
}
