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
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.DBConnection;

public class UpdateUtils {


  /*
   * changin orientation
   */
  public static boolean updateUserStructOrientation(int userStructId,
      StructOrientation orientation) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__ORIENTATION, orientation.getNumber());

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for setting a questitemtype as completed for a user quest
   */
  public static boolean updateUserQuestsSetCompleted(int userId, int questId, boolean setTasksCompleteTrue, boolean setDefeatTypeJobsCompleteTrue) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (setTasksCompleteTrue) {
      absoluteParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, true); 
    }
    if (setDefeatTypeJobsCompleteTrue) {
      absoluteParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, true); 
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for moving user structs
   */
  public static boolean updateUserCritstructCoord(int userId, CoordinatePair coordinates, CritStructType critStructType) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CRITSTRUCTS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (critStructType == CritStructType.ARMORY) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__ARMORY_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__ARMORY_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.VAULT) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__ARMORY_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__ARMORY_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.MARKETPLACE) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__MARKETPLACE_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__MARKETPLACE_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.LUMBERMILL) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__LUMBERMILL_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__LUMBERMILL_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.CARPENTER) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__CARPENTER_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__CARPENTER_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.AVIARY) {
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__AVIARY_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CRITSTRUCTS__AVIARY_Y_COORD, coordinates.getY());
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating is_complete=true and last_retrieved to upgrade_time+minutestogain for a userstruct
   */
  public static boolean updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(List<UserStruct> userStructs, int levelChange) {
    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();

    for (UserStruct userStruct : userStructs) {
      Structure structure = structures.get(userStruct.getStructId());
      if (structure == null) {
        return false;
      }
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getLastUpgradeTime().getTime() + 60000*structure.getMinutesToGain());
      if (!UpdateUtils.updateUserStructLastretrievedIscompleteLevelchange(userStruct.getId(), lastRetrievedTime, true, levelChange)) {
        return false;
      }
    }
    return true;
  }

  /*
   * used for updating last retrieved and/or last upgrade user struct time and is_complete
   */
  public static boolean updateUserStructLastretrievedIscompleteLevelchange(int userStructId, Timestamp lastRetrievedTime, boolean isComplete, int levelChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastRetrievedTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    absoluteParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, isComplete);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER_STRUCTS__LEVEL, levelChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_STRUCTS, relativeParams, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating is_complete=true and last_retrieved to purchased_time+minutestogain for a userstruct
   */
  public static boolean updateUserStructsLastretrievedpostbuildIscomplete(List<UserStruct> userStructs) {
    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();

    for (UserStruct userStruct : userStructs) {
      Structure structure = structures.get(userStruct.getStructId());
      if (structure == null) {
        return false;
      }
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getPurchaseTime().getTime() + 60000*structure.getMinutesToGain());
      if (!UpdateUtils.updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), lastRetrievedTime, null, true)) {
        return false;
      }
    }
    return true;
  }

  /*
   * used for updating last retrieved and/or last upgrade user struct time and is_complete
   */
  public static boolean updateUserStructLastretrievedLastupgradeIscomplete(int userStructId, Timestamp lastRetrievedTime, Timestamp lastUpgradeTime, boolean isComplete) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastRetrievedTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    if (lastUpgradeTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_UPGRADE_TIME, lastUpgradeTime);

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
   * used for upgrading user structs level
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
