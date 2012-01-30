package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.DBConnection;

public class UpdateUtils {

  /*
   * used for updating is_complete=true and last_retrieved to purchased_time+minutestogain for a userstruct
   */
  public static boolean updateUserStructsLastretrievedIscomplete(List<UserStruct> userStructs, boolean isComplete) {
    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();
    
    for (UserStruct userStruct : userStructs) {
      Structure structure = structures.get(userStruct.getId());
      if (structure == null) {
        return false;
      }
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getPurchaseTime().getTime() + 60000*structure.getMinutesToGain());
      if (!UpdateUtils.updateUserStructLastretrievedIscomplete(userStruct.getId(), lastRetrievedTime, true)) {
        return false;
      }
    }
    return true;
  }

  /*
   * used for updating last retrieved user struct time and is_complete
   */
  public static boolean updateUserStructLastretrievedIscomplete(int userStructId, Timestamp lastRetrievedTime, 
      boolean isComplete) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);
    absoluteParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, isComplete);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating last retrieved user struct time
   */
  public static boolean updateUserStructLastretrieved(int userStructId, Timestamp lastRetrievedTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for upgrading user structs
   */
  public static boolean updateUserStructLevel(int userStructId, int levelChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER_STRUCTS__LEVEL, levelChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, relativeParams, null, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for moving user structs
   */
  public static boolean updateUserStructCoord(int userStructId, CoordinatePair coordinates) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__X_COORD, coordinates.getX());
    absoluteParams.put(DBConstants.USER_STRUCTS__Y_COORD, coordinates.getY());

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for battles, tasks
   */
  public static boolean incrementUserEquip(int userId, int equipId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__QUANTITY, increment);
    insertParams.put(DBConstants.USER_EQUIP__IS_STOLEN, true);
    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_EQUIP, insertParams, 
        DBConstants.USER_EQUIP__QUANTITY, increment);
    if (numUpdated == 1 || numUpdated == 1*2) {
      return true;
    }
    return false;
  }

  //note: decrement is a positive number
  /*
   * used for battles
   */
  public static boolean decrementUserEquip(int userId, int equipId, int currentQuantity, int decrement) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    conditionParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);

    if (currentQuantity - decrement < 0) {
      return false;
    }
    if (currentQuantity - decrement <= 0) {
      int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_EQUIP, conditionParams, "and");
      if (numDeleted == 1) {
        return true;
      }
    } else {
      Map <String, Object> relativeParams = new HashMap<String, Object>();
      relativeParams.put(DBConstants.USER_EQUIP__QUANTITY, -1*decrement);      
      int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_EQUIP, relativeParams, null, 
          conditionParams, "and");
      if (numUpdated == 1) {
        return true;
      }
    }    
    return false;
  }

  /*
   * used for tasks
   */
  public static boolean incrementCityRankForUserCity(int userId, int cityId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_CITIES__USER_ID, userId);
    insertParams.put(DBConstants.USER_CITIES__CITY_ID, cityId);
    insertParams.put(DBConstants.USER_CITIES__CURRENT_RANK, increment);

    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_CITIES, insertParams, 
        DBConstants.USER_CITIES__CURRENT_RANK, increment);

    if (numUpdated == 1 || numUpdated == 1*2) {
      return true;
    }
    return false;
  }

  /*
   * used for tasks
   */
  public static boolean incrementTimesCompletedInRankForUserTask(int userId, int taskId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_TASK__USER_ID, userId);
    insertParams.put(DBConstants.USER_TASK__TASK_ID, taskId);
    insertParams.put(DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, increment);

    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_TASKS, insertParams, 
        DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, increment);

    if (numUpdated == 1 || numUpdated == 1*2) {
      return true;
    }
    return false;
  }  

  public static boolean resetTimesCompletedInRankForUserTasksInCity(int userId, List<Task> tasksInCity) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_TASK__USER_ID, userId);
    for (Task task : tasksInCity) {
      conditionParams.put(DBConstants.USER_TASK__TASK_ID, task.getId());
    }

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, 0);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_TASKS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == tasksInCity.size()) {
      return true;
    }
    return false;
  }

}
