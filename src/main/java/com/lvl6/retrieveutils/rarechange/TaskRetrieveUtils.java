package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Task;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.AnimationType;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class TaskRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<Task>> cityIdsToTasks;
  private static Map<Integer, Task> taskIdsToTasks;

  private static final String TABLE_NAME = DBConstants.TABLE_TASKS;

  public static Map<Integer, Task> getTaskIdsToTasks() {
    log.debug("retrieving all tasks data map");
    if (taskIdsToTasks == null) {
      setStaticTaskIdsToTasks();
    }
    return taskIdsToTasks;
  }

  public static Task getTaskForTaskId(int taskId) {
    log.debug("retrieve task data for task " + taskId);
    if (taskIdsToTasks == null) {
      setStaticTaskIdsToTasks();      
    }
    return taskIdsToTasks.get(taskId);
  }

  public static Map<Integer, Task> getTasksForTaskIds(List<Integer> ids) {
    log.debug("retrieve task data for taskids " + ids);
    if (taskIdsToTasks == null) {
      setStaticTaskIdsToTasks();      
    }
    Map<Integer, Task> toreturn = new HashMap<Integer, Task>();
    for (Integer id : ids) {
      toreturn.put(id,  taskIdsToTasks.get(id));
    }
    return toreturn;
  }

  public static List<Task> getAllTasksForCityId(int cityId) {
    log.debug("retrieving all tasks for cityId " + cityId);
    if (cityIdsToTasks == null) {
      setStaticCityIdsToTasks();
    }
    return cityIdsToTasks.get(cityId);
  }

  private static void setStaticCityIdsToTasks() {
    log.debug("setting static map of cityId to tasks");

    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map<Integer, List<Task>> cityIdToTasksTemp = new HashMap<Integer, List<Task>>();
          while(rs.next()) {  //should only be one
            Task task = convertRSRowToTask(rs);
            if (task != null) {
              if (cityIdToTasksTemp.get(task.getCityId()) == null) {
                cityIdToTasksTemp.put(task.getCityId(), new ArrayList<Task>());
              }
              cityIdToTasksTemp.get(task.getCityId()).add(task);
            }
          }
          cityIdsToTasks = cityIdToTasksTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  private static void setStaticTaskIdsToTasks() {
    log.debug("setting static map of taskIds to tasks");

    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, Task> taskIdsToTasksTemp = new HashMap<Integer, Task>();
          while(rs.next()) {  //should only be one
            Task task = convertRSRowToTask(rs);
            if (task != null)
              taskIdsToTasksTemp.put(task.getId(), task);
          }
          taskIdsToTasks = taskIdsToTasksTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticCityIdsToTasks();
    setStaticTaskIdsToTasks();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Task convertRSRowToTask(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    String goodName = rs.getString(i++);
    String badName = rs.getString(i++);
    int cityId = rs.getInt(i++);
    int energyCost = rs.getInt(i++);
    int minCoinsGained = rs.getInt(i++);
    int maxCoinsGained = rs.getInt(i++);
    float chanceOfEquipLoot = rs.getFloat(i++);
    String equipIdsString = rs.getString(i++);
    List<Integer> equipIds = new ArrayList<Integer>();
    if (equipIdsString != null) {
      StringTokenizer st = new StringTokenizer(equipIdsString, ", ");
      while (st.hasMoreTokens()) {
        equipIds.add(Integer.parseInt(st.nextToken()));
      }
    }
    int expGained = rs.getInt(i++);
    int assetNumWithinCity = rs.getInt(i++);
    int numForCompletion = rs.getInt(i++);
    String goodProcessingText = rs.getString(i++);
    String badProcessingText = rs.getString(i++);
    
    float spriteLandingX = rs.getFloat(i++);
    boolean spriteLandingXWasSet = !rs.wasNull();
    float spriteLandingY = rs.getFloat(i++);
    boolean spriteLandingYWasSet = !rs.wasNull();
    
    CoordinatePair spriteLandingCoords = (spriteLandingXWasSet && spriteLandingYWasSet) ? new CoordinatePair(spriteLandingX, spriteLandingY) : null;

    AnimationType at = AnimationType.valueOf(rs.getInt(i++));
    
    Task task = new Task(id, goodName, badName, cityId, energyCost, minCoinsGained, maxCoinsGained, 
        chanceOfEquipLoot, equipIds, expGained, assetNumWithinCity, numForCompletion, goodProcessingText, 
        badProcessingText, spriteLandingCoords, at);
    return task;
  }
}
