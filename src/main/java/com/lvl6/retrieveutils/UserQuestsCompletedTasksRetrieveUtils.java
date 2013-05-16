package com.lvl6.retrieveutils;

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

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserQuestsCompletedTasksRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS;

  
  ////@Cacheable(value="questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
  public Map<Integer, List<Integer>> getQuestIdToUserTasksCompletedForQuestForUser(int userId) {
    log.debug("retrieving user's quest id to completed tasks map for user " + userId);
    Map <Integer, List<Integer>> questIdToUserTasksCompleted = new HashMap<Integer, List<Integer>>();

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            Integer questId = rs.getInt(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID);
            Integer completedTaskId = rs.getInt(DBConstants.USER_QUESTS_COMPLETED_TASKS__COMPLETED_TASK_ID);
            if (questIdToUserTasksCompleted.get(questId) == null) {
              questIdToUserTasksCompleted.put(questId, new ArrayList<Integer>());
            }
            questIdToUserTasksCompleted.get(questId).add(completedTaskId);
          }
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      } 
    }
    DBConnection.get().close(rs, null, conn);
    
    return questIdToUserTasksCompleted;
  }
}
