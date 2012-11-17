package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;

import com.lvl6.info.ClanTower;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.UserStruct;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.DBConnection;

public class UpdateUtils implements UpdateUtil {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public static UpdateUtil get() {
    return (UpdateUtil) AppContext.getApplicationContext().getBean("updateUtils");
  }

  //  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());


  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserQuestsCoinsretrievedforreq(int, java.util.List, int)
   */
  @Override
  @Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
  public boolean updateUserQuestsCoinsretrievedforreq(int userId, List <Integer> questIds, int coinGain) {
    String query = "update " + DBConstants.TABLE_USER_QUESTS + " set " + DBConstants.USER_QUESTS__COINS_RETRIEVED_FOR_REQ
        + "=" + DBConstants.USER_QUESTS__COINS_RETRIEVED_FOR_REQ + "+? where " 
        + DBConstants.USER_QUESTS__USER_ID + "=? and (";
    List<Object> values = new ArrayList<Object>();
    values.add(coinGain);
    values.add(userId);
    List<String> condClauses = new ArrayList<String>();
    for (Integer questId : questIds) {
      condClauses.add(DBConstants.USER_QUESTS__QUEST_ID + "=?");
      values.add(questId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";
    int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
    if (numUpdated == questIds.size()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean updateAbsoluteBlacksmithAttemptcompleteTimeofspeedup(int blacksmithId, Date timeOfSpeedup, boolean attemptComplete) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.BLACKSMITH__ID, blacksmithId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.BLACKSMITH__ATTEMPT_COMPLETE, attemptComplete);

    if (timeOfSpeedup != null) {
      absoluteParams.put(DBConstants.BLACKSMITH__TIME_OF_SPEEDUP, timeOfSpeedup);
    }

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_BLACKSMITH, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateNullifyDeviceTokens(java.util.Set)
   */
  @Override
  public void updateNullifyDeviceTokens(Set<String> deviceTokens) {
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
      DBConnection.get().updateDirectQueryNaive(query, values);
    }
  }

  /*
   * used when an expansion is complete
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserExpansionNumexpansionsIsexpanding(int, int, int, boolean)
   */
  @Override
  public boolean updateUserExpansionNumexpansionsIsexpanding(int userId,
      int farLeftExpansionsChange, int farRightExpansionsChange, int nearLeftExpansionsChange, int nearRightExpansionsChange, 
      boolean isExpanding) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EXPANSIONS__USER_ID, userId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_EXPANSIONS__FAR_LEFT_EXPANSIONS, farLeftExpansionsChange);
    absoluteParams.put(DBConstants.USER_EXPANSIONS__FAR_RIGHT_EXPANSIONS, farRightExpansionsChange);
    absoluteParams.put(DBConstants.USER_EXPANSIONS__NEAR_LEFT_EXPANSIONS, nearLeftExpansionsChange);
    absoluteParams.put(DBConstants.USER_EXPANSIONS__NEAR_RIGHT_EXPANSIONS, nearRightExpansionsChange);
    absoluteParams.put(DBConstants.USER_EXPANSIONS__IS_EXPANDING, isExpanding);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_EXPANSIONS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for purchasing a city expansion
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(int, java.sql.Timestamp, com.lvl6.proto.InfoProto.ExpansionDirection, boolean)
   */
  @Override
  public boolean updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(int userId, Timestamp lastExpandTime, 
      ExpansionDirection lastExpansionDirection, boolean isExpanding) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EXPANSIONS__USER_ID, userId);
    insertParams.put(DBConstants.USER_EXPANSIONS__LAST_EXPAND_TIME, lastExpandTime);
    insertParams.put(DBConstants.USER_EXPANSIONS__LAST_EXPAND_DIRECTION, lastExpansionDirection.getNumber());
    insertParams.put(DBConstants.USER_EXPANSIONS__IS_EXPANDING, isExpanding);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_EXPANSIONS__LAST_EXPAND_TIME, lastExpandTime);
    absoluteParams.put(DBConstants.USER_EXPANSIONS__LAST_EXPAND_DIRECTION, lastExpansionDirection.getNumber());
    absoluteParams.put(DBConstants.USER_EXPANSIONS__IS_EXPANDING, isExpanding);

    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_EXPANSIONS, insertParams, null, absoluteParams);
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }


  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserQuestIscomplete(int, int)
   */
  @Override
  @Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
  public boolean updateUserQuestIscomplete(int userId, int questId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__IS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, true);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }  



  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateRedeemUserQuest(int, int)
   */
  @Override
  @Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
  public boolean updateRedeemUserQuest(int userId, int questId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_QUESTS__IS_REDEEMED, true);
    absoluteParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__IS_COMPLETE, true);
    absoluteParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, true);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * changin orientation
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructOrientation(int, com.lvl6.proto.InfoProto.StructOrientation)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructOrientation(int userStructId,
      StructOrientation orientation) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__ORIENTATION, orientation.getNumber());

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for setting a questitemtype as completed for a user quest
   */

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserQuestsSetCompleted(int, int, boolean, boolean)
   */
  @Override
  @Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
  public boolean updateUserQuestsSetCompleted(int userId, int questId, boolean setTasksCompleteTrue, boolean setDefeatTypeJobsCompleteTrue) {
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

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_QUESTS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }



  @Caching(evict= {
      @CacheEvict(value ="specificUserEquip", key="#userEquipId"),
      @CacheEvict(value="userEquipsForUser", key="#newOwnerId"),
      @CacheEvict(value="equipsToUserEquipsForUser", key="#newOwnerId"),
      @CacheEvict(value="userEquipsWithEquipId", key="#newOwnerId+':'+#equipId")  
  })
  public boolean updateUserEquipOwner(int userEquipId, int newOwnerId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EQUIP__ID, userEquipId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_EQUIP__USER_ID, newOwnerId); 

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_EQUIP, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating is_complete=true and last_retrieved to upgrade_time+minutestogain for a userstruct
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(java.util.List, int)
   */
  @Override
  public boolean updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(List<UserStruct> userStructs, int levelChange) {
    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();

    for (UserStruct userStruct : userStructs) {
      Structure structure = structures.get(userStruct.getStructId());
      if (structure == null) {
        return false;
      }
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getLastUpgradeTime().getTime() + 60000*MiscMethods.calculateMinutesToUpgradeForUserStruct(structure.getMinutesToUpgradeBase(), userStruct.getLevel()));
      if (!updateUserStructLastretrievedIscompleteLevelchange(userStruct.getId(), lastRetrievedTime, true, levelChange)) {
        return false;
      }
    }
    return true;
  }

  /*
   * used for updating last retrieved and/or last upgrade user struct time and is_complete
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructLastretrievedIscompleteLevelchange(int, java.sql.Timestamp, boolean, int)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructLastretrievedIscompleteLevelchange(int userStructId, Timestamp lastRetrievedTime, boolean isComplete, int levelChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastRetrievedTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    absoluteParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, isComplete);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER_STRUCTS__LEVEL, levelChange);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, relativeParams, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating is_complete=true and last_retrieved to purchased_time+minutestogain for a userstruct
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructsLastretrievedpostbuildIscomplete(java.util.List)
   */
  @Override
  public boolean updateUserStructsLastretrievedpostbuildIscomplete(List<UserStruct> userStructs) {
    Map<Integer, Structure> structures = StructureRetrieveUtils.getStructIdsToStructs();

    for (UserStruct userStruct : userStructs) {
      Structure structure = structures.get(userStruct.getStructId());
      if (structure == null) {
        return false;
      }
      Timestamp lastRetrievedTime = new Timestamp(userStruct.getPurchaseTime().getTime() + 60000*structure.getMinutesToBuild());
      if (!updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), lastRetrievedTime, null, true)) {
        return false;
      }
    }
    return true;
  }

  /*
   * used for updating last retrieved and/or last upgrade user struct time and is_complete
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructLastretrievedLastupgradeIscomplete(int, java.sql.Timestamp, java.sql.Timestamp, boolean)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructLastretrievedLastupgradeIscomplete(int userStructId, Timestamp lastRetrievedTime, Timestamp lastUpgradeTime, boolean isComplete) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastRetrievedTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    if (lastUpgradeTime != null)
      absoluteParams.put(DBConstants.USER_STRUCTS__LAST_UPGRADE_TIME, lastUpgradeTime);

    absoluteParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, isComplete);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for updating last retrieved user struct time
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructLastretrieved(int, java.sql.Timestamp)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructLastretrieved(int userStructId, Timestamp lastRetrievedTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, lastRetrievedTime);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for upgrading user structs level
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructLevel(int, int)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructLevel(int userStructId, int levelChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER_STRUCTS__LEVEL, levelChange);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, relativeParams, null, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for moving user structs
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#updateUserStructCoord(int, com.lvl6.info.CoordinatePair)
   */
  @Override
  @Caching(evict= {
      @CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      @CacheEvict(value="specificUserStruct", key="#userStructId")})
  public boolean updateUserStructCoord(int userStructId, CoordinatePair coordinates) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_STRUCTS__X_COORD, coordinates.getX());
    absoluteParams.put(DBConstants.USER_STRUCTS__Y_COORD, coordinates.getY());

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_STRUCTS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  /*
   * used for tasks
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#incrementCityRankForUserCity(int, int, int)
   */
  @Override
  @Caching(evict={@CacheEvict(value="cityIdToUserCityRankCache", key="#userId"),
      @CacheEvict(value="currentCityRankForUserCache", key="#userId+':'+#cityId")})
  public boolean incrementCityRankForUserCity(int userId, int cityId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_CITIES__USER_ID, userId);
    insertParams.put(DBConstants.USER_CITIES__CITY_ID, cityId);
    insertParams.put(DBConstants.USER_CITIES__CURRENT_RANK, increment);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_CITIES__CURRENT_RANK, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_CITIES, insertParams, 
        columnsToUpdate, null);//DBConstants.USER_CITIES__CURRENT_RANK, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean incrementNumberOfLockBoxesForLockBoxEvent(int userId, int eventId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_LOCK_BOX_EVENTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_LOCK_BOX_EVENTS__EVENT_ID, eventId);
    insertParams.put(DBConstants.USER_LOCK_BOX_EVENTS__NUM_BOXES, increment);
    insertParams.put(DBConstants.USER_LOCK_BOX_EVENTS__NUM_TIMES_COMPLETED, 0);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_LOCK_BOX_EVENTS__NUM_BOXES, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_LOCK_BOX_EVENTS, insertParams, 
    	columnsToUpdate, null);//DBConstants.USER_LOCK_BOX_EVENTS__NUM_BOXES, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean incrementQuantityForLockBoxItem(int userId, int itemId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_LOCK_BOX_ITEMS__USER_ID, userId);
    insertParams.put(DBConstants.USER_LOCK_BOX_ITEMS__ITEM_ID, itemId);
    insertParams.put(DBConstants.USER_LOCK_BOX_ITEMS__QUANTITY, increment);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_LOCK_BOX_ITEMS__QUANTITY, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_LOCK_BOX_ITEMS, insertParams, 
    	columnsToUpdate, null);//DBConstants.USER_LOCK_BOX_ITEMS__QUANTITY, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  } 

  /*
   * used for tasks
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#incrementTimesCompletedInRankForUserTask(int, int, int)
   */
  @Override
  public boolean incrementTimesCompletedInRankForUserTask(int userId, int taskId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_TASK__USER_ID, userId);
    insertParams.put(DBConstants.USER_TASK__TASK_ID, taskId);
    insertParams.put(DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, increment);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_TASKS, insertParams, 
    	columnsToUpdate, null);//DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }  


  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#incrementUserQuestDefeatTypeJobProgress(int, int, int, int)
   */
  @Override
  public boolean incrementUserQuestDefeatTypeJobProgress(int userId, int questId, int defeatTypeJobId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__DEFEAT_TYPE_JOB_ID, defeatTypeJobId);
    insertParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED, increment);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS, insertParams, 
        columnsToUpdate, null);//DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.UpdateUtil#incrementUserQuestTaskProgress(int, int, int, int)
   */
  @Override
  public boolean incrementUserQuestTaskProgress(int userId, int questId, int taskId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__TASK_ID, taskId);
    insertParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED, increment);

    Map<String, Object> columnsToUpdate = new HashMap<String, Object>();
    columnsToUpdate.put(DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED, increment);
    
    int numUpdated = DBConnection.get().insertOnDuplicateKeyUpdate(DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS, insertParams, 
    	columnsToUpdate, null);//DBConstants.USER_QUESTS_TASK_PROGRESS__NUM_TIMES_ACTED, increment);

    if (numUpdated >= 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean updateUsersClanId(Integer clanId, List<Integer> userIds) {
    String query = "update " + DBConstants.TABLE_USER + " set " + DBConstants.USER__CLAN_ID 
        + "=? where (" ;
    List<Object> values = new ArrayList<Object>();
    values.add(clanId);
    List<String> condClauses = new ArrayList<String>();
    for (Integer userId : userIds) {
      condClauses.add(DBConstants.USER__ID + "=?");
      values.add(userId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";
    int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
    if (numUpdated == userIds.size()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean updateClanOwnerDescriptionForClan(int clanId, int ownerId, String description) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.CLANS__ID, clanId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (ownerId > 0)
      absoluteParams.put(DBConstants.CLANS__OWNER_ID, ownerId);
    if (description != null)
      absoluteParams.put(DBConstants.CLANS__DESCRIPTION, description);

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_CLANS, null, absoluteParams, 
        conditionParams, "or");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean updateUserClanStatus(int userId, int clanId, UserClanStatus status) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CLANS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_CLANS__CLAN_ID, clanId);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER_CLANS__STATUS, status.getNumber());

    int numUpdated = DBConnection.get().updateTableRows(DBConstants.TABLE_USER_CLANS, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean resetTimesCompletedInRankForUserTasksInCity(int userId, List<Task> tasksInCity) {
    String query = "update " + DBConstants.TABLE_USER_TASKS + " set " + DBConstants.USER_TASK__NUM_TIMES_ACTED_IN_RANK 
        + "=? where " + DBConstants.USER_TASK__USER_ID + "=? and (" ;
    List<Object> values = new ArrayList<Object>();
    values.add(0);
    values.add(userId);
    List<String> condClauses = new ArrayList<String>();
    for (Task task : tasksInCity) {
      condClauses.add(DBConstants.USER_TASK__TASK_ID + "=?");
      values.add(task.getId());
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";
    int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
    if (numUpdated == tasksInCity.size()) {
      return true;
    }
    log.error("problem with resetting times completed in rank for userid " + userId + ". tasks are=" + tasksInCity
        + ", numUpdated=" + numUpdated);
    return false;
  }

  public boolean decrementLockBoxItemsForUser(Map<Integer, Integer> itemIdsToQuantity, int userId, int decrement) {
    String query = "update " + DBConstants.TABLE_USER_LOCK_BOX_ITEMS + " set " + DBConstants.USER_LOCK_BOX_ITEMS__QUANTITY 
        + "="+DBConstants.USER_LOCK_BOX_ITEMS__QUANTITY+"-? where " + DBConstants.USER_LOCK_BOX_ITEMS__USER_ID + "=? and " + 
        DBConstants.USER_LOCK_BOX_ITEMS__ITEM_ID+ " in (" ;
    List<Object> values = new ArrayList<Object>();
    values.add(decrement);
    values.add(userId);

    List<String> stringItems = new ArrayList<String>();
    for (Integer itemId : itemIdsToQuantity.keySet()) {
      stringItems.add("?");
      values.add(itemId);
    }
    query += StringUtils.getListInString(stringItems, ",") + ")";

    int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
    if (numUpdated != stringItems.size()) {
      return false;
    }

    values = new ArrayList<Object>();
    stringItems = new ArrayList<String>();
    for (Integer itemId : itemIdsToQuantity.keySet()) {
      int quantity = itemIdsToQuantity.get(itemId);
      if (quantity-decrement <= 0) {
        stringItems.add("?");
        values.add(itemId);
      }
    }

    if (stringItems.size() > 0) {
      query = "delete from " + DBConstants.TABLE_USER_LOCK_BOX_ITEMS + " where " +  DBConstants.USER_LOCK_BOX_ITEMS__ITEM_ID+ 
          " in (" + StringUtils.getListInString(stringItems, ",") + ") and " + DBConstants.USER_LOCK_BOX_ITEMS__USER_ID + "=?";
      values.add(userId);

      numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
      if (numUpdated == stringItems.size()) {
        return true;
      }
    } else {
      return true;
    }
    return false;
  }

  public boolean decrementNumLockBoxesIncrementNumTimesCompletedForUser(int eventId, int userId, int decrement, boolean completed, Timestamp curTime) {
    String query = "update " + DBConstants.TABLE_USER_LOCK_BOX_EVENTS + " set " + DBConstants.USER_LOCK_BOX_EVENTS__NUM_BOXES 
        + "="+DBConstants.USER_LOCK_BOX_EVENTS__NUM_BOXES +"-?, last_opening_time=?";

    List<Object> values = new ArrayList<Object>();
    values.add(decrement);
    values.add(curTime);
    if (completed) {
       query += ", "+DBConstants.USER_LOCK_BOX_EVENTS__NUM_TIMES_COMPLETED+ "="+DBConstants.USER_LOCK_BOX_EVENTS__NUM_TIMES_COMPLETED +"+?";
       values.add(1);
    }
    
    query += " where " + DBConstants.USER_LOCK_BOX_EVENTS__EVENT_ID + "=? and " + 
        DBConstants.USER_LOCK_BOX_EVENTS__USER_ID+ "=?" ;
    values.add(eventId);
    values.add(userId);

    int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
    if (numUpdated != 1) {
      return false;
    }
    return true;
  }

  /*
   * This function serves two purposes, modifying the owner part or the attacker part of
   * the table named clan_towers, since both parts are basically the same except in name.
   */
  public boolean updateClanTowerOwnerAndOrAttacker(int clanTowerId, 
		  int ownerId, Date ownedStartTime, int ownerBattleWins,
		  int attackerId, Date attackStartTime, int attackerBattleWins,
		  Date lastRewardGiven) {
	  String tablename = DBConstants.TABLE_CLAN_TOWERS;
	  String owner = DBConstants.CLAN_TOWERS__CLAN_OWNER_ID;
	  String ownedTime = DBConstants.CLAN_TOWERS__OWNED_START_TIME;
	  String ownerWins = DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS;
	  
	  String attacker = DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID;
	  String attackTime = DBConstants.CLAN_TOWERS__ATTACK_START_TIME;
	  String attackerWins = DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS;
	  
	  String lastRewardTime = DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN;

	  //the fields to change
	  Map<String, Object> absoluteParams = new HashMap<String,Object>();
	  absoluteParams.put(owner, ownerId);
	  absoluteParams.put(ownedTime, ownedStartTime);
	  absoluteParams.put(ownerWins, ownerBattleWins);
	  
	  absoluteParams.put(attacker, attackerId);
	  absoluteParams.put(attackTime, attackStartTime);
	  absoluteParams.put(attackerWins, attackerBattleWins);
	  
	  absoluteParams.put(lastRewardTime, lastRewardGiven);

	  //the tower being modified
	  Map<String, Object> conditionParams = new HashMap<String, Object>();
	  conditionParams.put(DBConstants.CLAN_TOWERS__TOWER_ID, clanTowerId);
	  
	  Map<String, Object> relativeParams = null;
	  String condDelim = "";
	  
	  int numUpdated = DBConnection.get().updateTableRows(tablename, relativeParams, 
			  absoluteParams, conditionParams, condDelim);
	  
	  if (1 != numUpdated) {
		  return false;
	  }
	  else {
		  return true;
	  }
  }
  
  //either updates the battle_wins for the owner of a clan tower
  //or the battle_wins for the attacker of a clan tower
  public boolean updateClanTowerBattleWins(int clanTowerId, int ownerId, int attackerId,
		  boolean ownerWon, int amountToIncrementBattleWinsBy) {
	  String tableName = DBConstants.TABLE_CLAN_TOWERS;
	  
	  String ownerOrAttackerBattleWins = "";
	  
	  if (ownerWon) {
		  ownerOrAttackerBattleWins = DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS;
	  }
	  else {
		  ownerOrAttackerBattleWins = DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS;
	  }
	  
	  Map<String, Object> relativeParams = new HashMap<String, Object>();
	  relativeParams.put(ownerOrAttackerBattleWins, amountToIncrementBattleWinsBy);
	  
	  Map<String, Object> conditionParams = new HashMap<String, Object>();
	  conditionParams.put(DBConstants.CLAN_TOWERS__TOWER_ID, clanTowerId);
	  conditionParams.put(DBConstants.CLAN_TOWERS__CLAN_OWNER_ID, ownerId);
	  conditionParams.put(DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID, attackerId);
	  
	  Map<String, Object> absoluteParams = null;
	  String condDelim = "AND";
	  
	  int numUpdated = DBConnection.get().updateTableRows(tableName, relativeParams, 
			  absoluteParams, conditionParams, condDelim);
	  
	  //a clan can own multiple towers with another clan being the same attacker for all of them
	  if (0 == numUpdated) {
		  return false; //there should be a tower with an owner and an attacker with the specified ids 
	  }
	  else {
		  return true;
	  }
  }
  
  public void resetClanTowerOwnerOrAttacker(int clanTowerOwnerOrAttackerId, boolean resetOwner) {
	  String tableName = DBConstants.TABLE_CLAN_TOWERS;

	  List<Object> values = new ArrayList<Object>();
	  String ownerOrAttacker = "";
	  String ownedStartTime = "";
	  String ownerBattleWins = "";
	  String attackStartTime = "";
	  String attackerBattleWins = "";
	  String lastRewardTime = "";
	  String whereClause = "";
	  if(resetOwner) {
		  //if the clan_towers.clanOwnerId = (clan id of clan who lost a member) 
		  //set the clanOwnerId to the clanAttackerId, regardless of whether it is set
		  ownerOrAttacker = DBConstants.CLAN_TOWERS__CLAN_OWNER_ID + "=" +
				  DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID + ",";
		  
		  //changed ownership, reset time and battle wins
		  ownedStartTime = " " + DBConstants.CLAN_TOWERS__OWNED_START_TIME + "=?,";
		  //if there is no attacker to take ownership this should be null, but doesn't harm anything
		  values.add(new Timestamp(new Date().getTime()));
		  ownerBattleWins = " " + DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS + "=?,";
		  values.add(0);

		  attackStartTime = " " + DBConstants.CLAN_TOWERS__ATTACK_START_TIME + "=?,";
		  values.add(null);
		  attackerBattleWins = " " + DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS + "=?,";
		  values.add(0);
		  
		  //reset last reward given
		  lastRewardTime = " " + DBConstants.CLAN_TOWERS__LAST_REWARD_GIVEN + "=?";
		  values.add(null);
		  
		  whereClause = " where " + DBConstants.CLAN_TOWERS__CLAN_OWNER_ID + "=?";
		  values.add(clanTowerOwnerOrAttackerId);
	  }
	  else {
		  ownerOrAttacker = DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID + "=?,";
		  values.add(null);
		  
		  //no attacker means reset battle wins
		  ownerBattleWins = " " + DBConstants.CLAN_TOWERS__OWNER_BATTLE_WINS + "=?,";
		  values.add(0);
		  attackerBattleWins = " " + DBConstants.CLAN_TOWERS__ATTACKER_BATTLE_WINS + "=?,";
		  values.add(0);
		  
		  attackStartTime = " " + DBConstants.CLAN_TOWERS__ATTACK_START_TIME + "=?";
		  values.add(null);
		  
		  whereClause = " where " + DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID + "=?";
		  values.add(clanTowerOwnerOrAttackerId);
	  }
	  String query = "update " + tableName + " set "  
			    + ownerOrAttacker
		        + ownedStartTime + ownerBattleWins
		        + attackStartTime + attackerBattleWins
		        + lastRewardTime
		        + whereClause ;
	  
	  int numUpdated = DBConnection.get().updateDirectQueryNaive(query, values);
	  //don't really care about the updates, because the desired rows affected
	  //could be 0 or more. All that matters is that it happens.
  }
  
  public void updateTowerHistory(JdbcTemplate jdbcTemplate, ClanTower tower, String reasonForEntry) {
		jdbcTemplate.execute("insert into "+DBConstants.TABLE_CLAN_TOWERS_HISTORY
				+" ("
				+DBConstants.CLAN_TOWERS_HISTORY__OWNER_CLAN_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_CLAN_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__TOWER_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACK_START_TIME
				+DBConstants.CLAN_TOWERS_HISTORY__WINNER_ID+", "
				+DBConstants.CLAN_TOWERS_HISTORY__OWNER_BATTLE_WINS+", "
				+DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_BATTLE_WINS+", "
				+DBConstants.CLAN_TOWERS_HISTORY__NUM_HOURS_FOR_BATTLE+", "
				+DBConstants.CLAN_TOWERS_HISTORY__LAST_REWARD_GIVEN+", "
				+DBConstants.CLAN_TOWERS_HISTORY__REASON_FOR_ENTRY+", "
				+") VALUES ("
				+tower.getClanOwnerId()+", "
				+tower.getClanAttackerId()+","
				+tower.getId()+", "
				+tower.getAttackStartTime()+", "
				+(tower.getAttackerBattleWins() > tower.getOwnerBattleWins() ? tower.getClanAttackerId() : tower.getClanOwnerId())+","
				+tower.getOwnerBattleWins()+", "
				+tower.getAttackerBattleWins()+", "
				+tower.getNumHoursForBattle()+", "
				+tower.getLastRewardGiven()+", "
				+reasonForEntry+")"
		);
	}
}
