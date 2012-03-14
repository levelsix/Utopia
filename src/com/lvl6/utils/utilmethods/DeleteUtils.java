package com.lvl6.utils.utilmethods;

import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class DeleteUtils {

  public static boolean deleteAvailableReferralCode(String referralCode) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.AVAILABLE_REFERRAL_CODES__CODE, referralCode);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_AVAILABLE_REFERRAL_CODES, conditionParams, "and");
    if (numDeleted != 1) {
      return false;
    }
    return true;  
  }

  public static boolean deleteUserQuestInfoInTaskProgressAndCompletedTasks(int userId, int questId, int numTasks) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID, questId);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, conditionParams, "and");
    if (numDeleted != numTasks) {
      return false;
    }

    conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_TASK_PROGRESS__QUEST_ID, questId);

    numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_QUESTS_TASK_PROGRESS, conditionParams, "and");
    if (numDeleted != numTasks) {
      return false;
    }
    return true;  
  }

  public static boolean deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(int userId, int questId, int numDefeatJobs) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID, questId);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS, conditionParams, "and");
    if (numDeleted != numDefeatJobs) {
      return false;
    }
    
    conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    conditionParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID, questId);

    numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS, conditionParams, "and");
    if (numDeleted != numDefeatJobs) {
      return false;
    }
    return true;  
  }
  
  public static boolean deleteMarketplacePost(int mpId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.MARKETPLACE__ID, mpId);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_MARKETPLACE, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }

    return false;
  }
  
  public static boolean deleteUserStruct(int userStructId) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_STRUCTS__ID, userStructId);

    int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_STRUCTS, conditionParams, "and");
    if (numDeleted == 1) {
      return true;
    }

    return false;
  }
}
