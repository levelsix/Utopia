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

  private static Map<Integer, List<Task>> cityIdToTasks;
  private static Map<Integer, Task> taskIdsToTask;
  
  private static final String TABLE_NAME = DBConstants.TABLE_TASKS;
    
  public static Map<Integer, Task> getAllTaskIdsToTask() {
    log.info("retrieving all-tasks data");
    if (taskIdsToTask == null) {
      setStaticTaskIdsToTask();
    }
    return taskIdsToTask;
  }
  
  public static Task getTaskForTaskId(int taskId) {
    log.info("retrieve task data");
    if (taskIdsToTask == null) {
      setStaticTaskIdsToTask();      
    }
    return taskIdsToTask.get(taskId);
  }
  
  public static List<Task> getAllTasksForCityId(int cityId) {
    if (cityIdToTasks == null) {
      setStaticCityIdsToTasks();
    }
    return cityIdToTasks.get(cityId);
  }
  
  private static void setStaticCityIdsToTasks() {
    log.info("setting static map of taskIds to task");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<Task>> cityIdToTasksTemp = new HashMap<Integer, List<Task>>();
        while(rs.next()) {  //should only be one
          Task task = convertRSRowToEquipment(rs);
          if (task != null) {
            if (cityIdToTasksTemp.get(task.getCityId()) == null) {
              cityIdToTasksTemp.put(task.getCityId(), new ArrayList<Task>());
            }
            cityIdToTasksTemp.get(task.getCityId()).add(task);
          }
        }
        cityIdToTasks = cityIdToTasksTemp;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }    
  }
  
  private static void setStaticTaskIdsToTask() {
    log.info("setting static map of equipIds to equipment");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        taskIdsToTask = new HashMap<Integer, Task>();
        while(rs.next()) {  //should only be one
          Task task = convertRSRowToEquipment(rs);
          if (task != null)
            taskIdsToTask.put(task.getId(), task);
        }
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }    
  }
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Task convertRSRowToEquipment(ResultSet rs) throws SQLException {
    int id = rs.getInt(1);
    String goodName = rs.getString(2);
    String badName = rs.getString(3);
    int cityId = rs.getInt(4);
    int energyCost = rs.getInt(5);
    int minCoinsGained = rs.getInt(6);
    int maxCoinsGained = rs.getInt(7);
    float chanceOfEquipLoot = rs.getFloat(8);
    String equipIdsString = rs.getString(9);
    List<Integer> equipIds = new ArrayList<Integer>();
    StringTokenizer st = new StringTokenizer(equipIdsString, ", ");
    while (st.hasMoreTokens()) {
      equipIds.add(Integer.parseInt(st.nextToken()));
    }
    int expGained = rs.getInt(10);
    int minArmySize = rs.getInt(11);
    int assetNumWithinCity = rs.getInt(12);
    int numForCompletion = rs.getInt(13);
    
    Task task = new Task(id, goodName, badName, cityId, energyCost, minCoinsGained, maxCoinsGained, 
        chanceOfEquipLoot, equipIds, expGained, minArmySize, assetNumWithinCity, numForCompletion);
    return task;
  }
}
