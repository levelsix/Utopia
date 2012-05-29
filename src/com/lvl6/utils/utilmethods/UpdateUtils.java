package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.DBConnection;

public class UpdateUtils {

//  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  public static void updateNullifyDeviceTokens(Set<String> deviceTokens) {
    if (deviceTokens != null && deviceTokens.size() > 0) {
      String query = "update " + DBConstants.TABLE_USER + " set " + DBConstants.USER__DEVICE_TOKEN 
          + "=? where ";
      List<Object> values = new ArrayList<Object>();
      values.add(null);
      List<String> condClauses = new ArrayList<String>();
      for (String deviceToken : deviceTokens) {
        condClauses.add(DBConstants.USER__DEVICE_TOKEN + "=?");
        values.add(deviceToken);
      }
      query += StringUtils.getListInString(condClauses, "or");
      DBConnection.updateDirectQueryNaive(query, values);
    }
  }

  /*
   * used when an expansion is complete
   */
  public static boolean updateUserExpansionNumexpansionsIsexpanding(int userId,
      int farLeftExpansionsChange, int farRightExpansionsChange, 
      boolean isExpanding) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__FAR_LEFT_EXPANSIONS, farLeftExpansionsChange);
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__FAR_RIGHT_EXPANSIONS, farRightExpansionsChange);
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__IS_EXPANDING, isExpanding);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_CITY_ELEMS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for purchasing a city expansion
   */
  public static boolean updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(int userId, Timestamp lastExpandTime, 
      ExpansionDirection lastExpansionDirection, boolean isExpanding) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__LAST_EXPAND_TIME, lastExpandTime);
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__LAST_EXPAND_DIRECTION, lastExpansionDirection.getNumber());
    absoluteParams.put(DBConstants.USER_CITY_ELEMS__IS_EXPANDING, isExpanding);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_CITY_ELEMS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  public static boolean updateUserQuestIscomplete(int userId, int questId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__IS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, true);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }  

  public static boolean updateRedeemUserQuest(int userId, int questId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_QUESTS__IS_REDEEMED, true);
    absoluteParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__IS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, true);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

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
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  public static boolean updateUserCritstructOrientation(int userId, StructOrientation orientation, CritStructType critStructType) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (critStructType == CritStructType.ARMORY) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__ARMORY_ORIENTATION, orientation.getNumber());
    }
    if (critStructType == CritStructType.VAULT) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__VAULT_ORIENTATION, orientation.getNumber());
    }
    if (critStructType == CritStructType.MARKETPLACE) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__MARKETPLACE_ORIENTATION, orientation.getNumber());
    }
    if (critStructType == CritStructType.CARPENTER) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_ORIENTATION, orientation.getNumber());
    }
    if (critStructType == CritStructType.AVIARY) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_ORIENTATION, orientation.getNumber());
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_CITY_ELEMS, null, absoluteParams, 
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
    conditionParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (critStructType == CritStructType.ARMORY) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__ARMORY_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__ARMORY_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.VAULT) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__VAULT_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__VAULT_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.MARKETPLACE) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__MARKETPLACE_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__MARKETPLACE_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.CARPENTER) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_Y_COORD, coordinates.getY());
    }
    if (critStructType == CritStructType.AVIARY) {
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_X_COORD, coordinates.getX());
      absoluteParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_Y_COORD, coordinates.getY());
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_CITY_ELEMS, null, absoluteParams, 
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
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getLastUpgradeTime().getTime() + 60000*MiscMethods.calculateMinutesToUpgradeForUserStruct(structure.getMinutesToUpgradeBase(), userStruct.getLevel()));
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
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getPurchaseTime().getTime() + 60000*structure.getMinutesToBuild());
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

  public static boolean incrementUserEquip(int userId, int equipId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__QUANTITY, increment);
    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_EQUIP, insertParams, 
        DBConstants.USER_EQUIP__QUANTITY, increment);
    if (numUpdated >= 1) {
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

    if (numUpdated >= 1) {
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

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }  


  public static boolean incrementUserQuestDefeatTypeJobProgress(int userId, int questId, int defeatTypeJobId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__DEFEAT_TYPE_JOB_ID, defeatTypeJobId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED, increment);

    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS, insertParams, 
        DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  public static boolean incrementUserQuestTaskProgress(int userId, int questId, int taskId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__TASK_ID, taskId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED, increment);

    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS, insertParams, 
        DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  public static boolean resetTimesCompletedInRankForUserTasksInCity(int userId, List<Task> tasksInCity) {
    String query = "update " + DBConstants.TABLE_USER_TASKS + " set " + DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK 
        + "=? where ";
    List<Object> values = new ArrayList<Object>();
    values.add(0);
    List<String> condClauses = new ArrayList<String>();
    for (Task task : tasksInCity) {
      condClauses.add(DBConstants.USER_TASK__TASK_ID + "=?");
      values.add(task.getId());
    }
    query += StringUtils.getListInString(condClauses, "or");
    int numUpdated = DBConnection.updateDirectQueryNaive(query, values);
    if (numUpdated == tasksInCity.size()) {
      return true;
    }
    return false;
  }

}
