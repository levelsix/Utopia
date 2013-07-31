package com.lvl6.utils.utilmethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.DBConnection;

public class DeleteUtils implements DeleteUtil {


  public static DeleteUtil get(){
    return (DeleteUtil) AppContext.getApplicationContext().getBean("deleteUtils");
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.DeleteUtil#deleteAvailableReferralCode(java.lang.String)
   */
  @Override
  public boolean deleteAvailableReferralCode(String referralCode) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.AVAILABLE_REFERRAL_CODES__CODE, referralCode);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_AVAILABLE_REFERRAL_CODES, conditionParams, "and");
    if (numDeleted != 1) {
      return false;
    }
    return true;  
  }

  
  ////@CacheEvict(value ="specificUserEquip", key="#userEquipId")
  public boolean deleteUserEquip(int userEquipId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EQUIP__ID, userEquipId);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_EQUIP, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }
    return false;
  }
  
  public boolean deleteUserEquips(List<Integer> userEquipIds) {
    String tableName = DBConstants.TABLE_USER_EQUIP;
    List<String> questions = new ArrayList<String>();
    for(int userEquipId : userEquipIds) {
      questions.add("?");
    }
    
    String delimiter = ",";
    String query = " DELETE FROM " + tableName + " WHERE " + DBConstants.USER_EQUIP__ID
    + " IN (" + StringUtils.getListInString(questions, delimiter) + ")";
    
    List values = userEquipIds; //adding generics will throw (type mismatch?) errors
    
    int numDeleted = DBConnection.get().deleteDirectQueryNaive(query, values);
    if(userEquipIds.size() == numDeleted) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean deleteBlacksmithAttempt(int blacksmithId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.BLACKSMITH__ID, blacksmithId);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_BLACKSMITH, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.DeleteUtil#deleteUserQuestInfoInTaskProgressAndCompletedTasks(int, int, int)
   */
  @Override
 // //@CacheEvict(value = "questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
  public boolean deleteUserQuestInfoInTaskProgressAndCompletedTasks(int userId, int questId, int numTasks) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID, questId);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, conditionParams, "and");
    if (numDeleted != numTasks) {
      return false;
    }

    conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__QUEST_ID, questId);

    numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS, conditionParams, "and");
    if (numDeleted != numTasks) {
      return false;
    }
    return true;  
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.DeleteUtil#deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(int, int, int)
   */
  @Override
  ////@CacheEvict(value="questIdToUserDefeatTypeJobsCompletedForQuestForUserCache", key="#userId")
  public boolean deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(int userId, int questId, int numDefeatJobs) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID, questId);

    //trust?
    DBConnection.get().deleteRows(DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS, conditionParams, "and");

    conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID, questId);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS, conditionParams, "and");
    if (numDeleted != numDefeatJobs) {
      return false;
    }
    return true;  
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.DeleteUtil#deleteMarketplacePost(int)
   */
  @Override
  public boolean deleteMarketplacePost(int mpId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.MARKETPLACE__ID, mpId);

    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_MARKETPLACE, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.DeleteUtil#deleteUserStruct(int)
   */
  @Override
  /*@Caching(evict= {
      //@CacheEvict(value="structIdsToUserStructsForUser", allEntries=true),
      //@CacheEvict(value="specificUserStruct", key="#userStructId")})*/
  public boolean deleteUserStruct(int userStructId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);
    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_STRUCTS, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }
    return false;
  }
  
  public boolean deleteUserClanDataRelatedToClanId(int clanId, int numRowsToDelete) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_CLANS, conditionParams, "and");
    if (numDeleted == numRowsToDelete) {
      return true;
    }
    return false;
  }
  
  public void deleteUserClansForUserExceptSpecificClan(int userId, int clanId) {
    String query = "delete from " + DBConstants.TABLE_USER_CLANS + " where " + DBConstants.USER_CLANS__USER_ID + "=? and " +
        DBConstants.USER_CLANS__CLAN_ID + "!=?";
    
    List<Object> values = new ArrayList<Object>();
    values.add(userId);
    values.add(clanId);
    
    DBConnection.get().deleteDirectQueryNaive(query, values);
  }
  
  
  public boolean deleteUserClan(int userId, int clanId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    conditionParams.put(DBConstants.USER_CLANS__USER_ID, userId);
    
    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_USER_CLANS, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }
    return false;
  }

  
  //@CacheEvict(value="clanById", key="#clanId")
  public boolean deleteClanWithClanId(int clanId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.CLANS__ID, clanId);
    int numDeleted = DBConnection.get().deleteRows(DBConstants.TABLE_CLANS, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }
    return false;
  }
  
  public boolean deleteEquipEnhancements(List<Integer> equipEnhancementIds) {
    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT;
    List<String> questions = new ArrayList<String>();
    for(int id : equipEnhancementIds) {
      questions.add("?");
    }
    
    String delimiter = ",";
    String query = " DELETE FROM " + tableName + " WHERE " + DBConstants.EQUIP_ENHANCEMENT__ID 
    + " IN (" + StringUtils.getListInString(questions, delimiter) + ")";
    
    List values = equipEnhancementIds; //adding generics will throw (type mismatch?) errors
    
    int numDeleted = DBConnection.get().deleteDirectQueryNaive(query, values);
    if(equipEnhancementIds.size() == numDeleted) {
      return true;
    } else {
      return false;
    }
  }
  
  //since many EquipEnhancementFeeders point to one EquipEnhancement, could just delete by EnhancementId
  public boolean deleteEquipEnhancementFeeders(List<Integer> equipEnhancementFeederIds) {
    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS;
    List<String> questions = new ArrayList<String>();
    for(int id : equipEnhancementFeederIds) {
      questions.add("?");
    }
    
    String delimiter = ",";
    String query = " DELETE FROM " + tableName + " WHERE " + DBConstants.EQUIP_ENHANCEMENT__ID 
    + " IN (" + StringUtils.getListInString(questions, delimiter) + ")";
    
    List values = equipEnhancementFeederIds; //adding generics will throw (type mismatch?) errors
    
    int numDeleted = DBConnection.get().deleteDirectQueryNaive(query, values);
    if(equipEnhancementFeederIds.size() == numDeleted) {
      return true;
    } else {
      return false;
    }
  }

  public int deleteAllUserCitiesForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_CITIES;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_CITIES__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserQuestsForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_QUESTS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserQuestsCompletedDefeatTypeJobsForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserQuestsCompletedTasksForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserQuestsDefeatTypeJobProgressForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserQuestsTaskProgress(int userId) {
    String tableName = DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  public int deleteAllUserTasksForUser(int userId) {
    String tableName = DBConstants.TABLE_USER_TASKS;
    String condDelim = "and";
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_TASK__USER_ID, userId);
    int numDeleted = DBConnection.get().deleteRows(tableName, conditionParams, condDelim);
    
    return numDeleted;
  }
  
}
