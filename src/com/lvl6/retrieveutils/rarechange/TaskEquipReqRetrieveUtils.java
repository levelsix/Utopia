package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.TaskEquipRequirement;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class TaskEquipReqRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Map<Integer, Integer>> taskIdToEquipmentIdQuantityMap;
  
  private static final String TABLE_NAME = DBConstants.TABLE_TASKS_EQUIPREQS;

  
  public static Map<Integer, Integer> getEquipmentIdsToQuantityForTaskId(int taskId) {
    log.info("retrieving equipment reqs for task with id " + taskId);
    
    if (taskIdToEquipmentIdQuantityMap == null) {
      setStaticTaskIdToEquipmentIdQuantityMap();
    }
    return taskIdToEquipmentIdQuantityMap.get(taskId);
  }


  private static void setStaticTaskIdToEquipmentIdQuantityMap() {
    log.info("setting static map of taskId to equipment/quantity map");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Map<Integer, Integer>> taskIdToEquipmentIdQuantityTemp = new HashMap<Integer, Map<Integer, Integer>>();
        while(rs.next()) {  //should only be one
          TaskEquipRequirement ter = convertRSRowToTaskEquipReq(rs);
          if (ter != null) {
            Map<Integer, Integer> equipmentIdToQuantity = taskIdToEquipmentIdQuantityTemp.get(ter.getTaskId());
            if (equipmentIdToQuantity != null) {
              equipmentIdToQuantity.put(ter.getEquipId(), ter.getQuantity());
            } else {
              Map <Integer, Integer> toInsert = new HashMap<Integer, Integer>();
              toInsert.put(ter.getEquipId(), ter.getQuantity());
              taskIdToEquipmentIdQuantityTemp.put(ter.getTaskId(), toInsert);
            }
          }
        }
        taskIdToEquipmentIdQuantityMap = taskIdToEquipmentIdQuantityTemp;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }
  }

  private static TaskEquipRequirement convertRSRowToTaskEquipReq(ResultSet rs) throws SQLException {
    int taskId = rs.getInt(1);
    int equipId = rs.getInt(2);
    int quantity = rs.getInt(3);
    return new TaskEquipRequirement(taskId, equipId, quantity);
  }
  
}
