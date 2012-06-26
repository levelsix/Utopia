package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserTaskRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_TASKS;
  
  public static Map<Integer, Integer> getTaskIdToNumTimesActedInRankForUser(int userId) {
    log.debug("retrieving task id to num times acted in rank map for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Integer> taskIdToNumTimesActedInRank = convertRSToTaskIdToNumTimesCompletedMap(rs);
    DBConnection.get().close(rs, null, conn);
    return taskIdToNumTimesActedInRank;
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
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
}
