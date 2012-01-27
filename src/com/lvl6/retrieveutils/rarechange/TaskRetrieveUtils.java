package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.lvl6.info.Task;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class TaskRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<Task>> cityIdsToTasks;
  private static Map<Integer, Task> taskIdsToTasks;

  private static final String TABLE_NAME = DBConstants.TABLE_TASKS;

  public static Map<Integer, Task> getTaskIdsToTasks() {
    log.info("retrieving all-tasks data");
    if (taskIdsToTasks == null) {
      setStaticTaskIdsToTasks();
    }
    return taskIdsToTasks;
  }

  public static Task getTaskForTaskId(int taskId) {
    log.info("retrieve task data");
    if (taskIdsToTasks == null) {
      setStaticTaskIdsToTasks();      
    }
    return taskIdsToTasks.get(taskId);
  }

  public static List<Task> getAllTasksForCityId(int cityId) {
    if (cityIdsToTasks == null) {
      setStaticCityIdsToTasks();
    }
    return cityIdsToTasks.get(cityId);
  }

  private static void setStaticCityIdsToTasks() {
    log.info("setting static map of cityId to tasks");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);

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

  private static void setStaticTaskIdsToTasks() {
    log.info("setting static map of taskIds to tasks");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
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

    Task task = new Task(id, goodName, badName, cityId, energyCost, minCoinsGained, maxCoinsGained, 
        chanceOfEquipLoot, equipIds, expGained, assetNumWithinCity, numForCompletion);
    return task;
  }
}
