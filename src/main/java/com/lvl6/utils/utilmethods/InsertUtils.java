package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.log.Log;

import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.DBConstants;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.EventProto.EarnFreeDiamondsRequestProto.AdColonyRewardType;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.DBConnection;

public class InsertUtils implements InsertUtil{


  public static InsertUtil get() {
    return (InsertUtil) AppContext.getApplicationContext().getBean("insertUtils");
  }

  //	@Autowired
  //	protected CacheManager cache;

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#getCache()
   */
  //	@Override
  //	public CacheManager getCache() {
  //		return cache;
  //	}

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#setCache(org.springframework.cache.CacheManager)
   */
  //	@Override
  //	public void setCache(CacheManager cache) {
  //		this.cache = cache;
  //	}

  public boolean insertLastLoginLastLogoutToUserSessions(int userId, Timestamp loginTime, Timestamp logoutTime) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_SESSIONS__USER_ID, userId);
    insertParams.put(DBConstants.USER_SESSIONS__LOGIN_TIME, loginTime);
    insertParams.put(DBConstants.USER_SESSIONS__LOGOUT_TIME, logoutTime);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_SESSIONS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  public boolean insertForgeAttemptIntoBlacksmithHistory(BlacksmithAttempt ba, boolean successfulForge) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__ID, ba.getId());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__USER_ID, ba.getUserId());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__EQUIP_ID, ba.getEquipId());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__GOAL_LEVEL, ba.getGoalLevel());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__GUARANTEED, ba.isGuaranteed());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__START_TIME, ba.getStartTime());

    if (ba.getDiamondGuaranteeCost() > 0) {
      insertParams.put(DBConstants.BLACKSMITH_HISTORY__DIAMOND_GUARANTEE_COST, ba.getDiamondGuaranteeCost());
    }

    if (ba.getTimeOfSpeedup() != null) {
      insertParams.put(DBConstants.BLACKSMITH_HISTORY__TIME_OF_SPEEDUP, ba.getTimeOfSpeedup());
    }

    insertParams.put(DBConstants.BLACKSMITH_HISTORY__SUCCESS, successfulForge);

    insertParams.put(DBConstants.BLACKSMITH_HISTORY__EQUIP_ONE_ENHANCEMENT_PERCENT,
        ba.getEquipOneEnhancementPercent());
    insertParams.put(DBConstants.BLACKSMITH_HISTORY__EQUIP_TWO_ENHANCEMENT_PERCENT,
        ba.getEquipTwoEnhancementPercent());
    
    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_BLACKSMITH_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertUserEquip(int, int)
   */
  @Override
  /*@Caching(evict = {
      @CacheEvict(value = "userEquipsForUser", key = "#userId"),
      @CacheEvict(value = "equipsToUserEquipsForUser", key = "#userId"),
      @CacheEvict(value = "userEquipsWithEquipId", key = "#userId+':'+#equipId") })*/
  public int insertUserEquip(int userId, int equipId, int level) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__LEVEL, level);

    int userEquipId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_USER_EQUIP, insertParams);
    return userEquipId;
  }
  
  public int insertUserEquip(int userId, int equipId, int level, int enhancementPercentage) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__LEVEL, level);
    insertParams.put(DBConstants.USER_EQUIP__ENHANCEMENT_PERCENT, enhancementPercentage);

    int userEquipId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_USER_EQUIP, insertParams);
    return userEquipId;
  }
  
  public int insertEquipEnhancement(int userId, int equipId, int equipLevel,
      int enhancementPercentageBeforeEnhancement, Timestamp startTimeOfEnhancement) {
    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT__USER_ID, userId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT__EQUIP_ID, equipId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT__EQUIP_LEVEL, equipLevel);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT__ENHANCEMENT_PERCENTAGE_BEFORE_ENHANCEMENT,
        enhancementPercentageBeforeEnhancement);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT__START_TIME_OF_ENHANCEMENT, startTimeOfEnhancement);
    
    int equipEnhancementId = DBConnection.get().insertIntoTableBasicReturnId(tableName, insertParams);
    return equipEnhancementId;
  }
  
  public int insertIntoEquipEnhancementHistory(int equipEnhancementId, int userId, int equipId, 
      int equipLevel, int currentEnhancementPercentage, int previousEnhancementPercentage, 
      Timestamp startTimeOfEnhancement, Timestamp timeOfSpeedup, int userEquipId) {

    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT_HISTORY;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__EQUIP_ENHANCEMENT_ID, equipEnhancementId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__EQUIP_ID, equipId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__EQUIP_LEVEL, equipLevel);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__CURRENT_ENHANCEMENT_PERCENTAGE, 
        currentEnhancementPercentage);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__PREVIOUS_ENHANCEMENT_PERCENTAGE, 
        previousEnhancementPercentage);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__START_TIME_OF_ENHANCEMENT,
        startTimeOfEnhancement);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__TIME_OF_SPEED_UP, timeOfSpeedup);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__RESULTING_USER_EQUIP_ID, userEquipId);
    
    int numInserted = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    return numInserted;
  }
  
  //many equip enhancement feeders to one equip enhancement id
  public List<Integer> insertEquipEnhancementFeeders(int equipEnhancementId, List<UserEquip> feeders) {
    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS;
    List<Map<String, Object>> newRows = new ArrayList<Map<String, Object>>();
    for(UserEquip ue: feeders) {
      int equipId = ue.getEquipId();
      int equipLevel = ue.getLevel();
      int enhancementPercentageBeforeEnhancement = ue.getEnhancementPercentage();
          
      Map<String, Object> oneRow = new HashMap<String, Object>();
      oneRow.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS__EQUIP_ENHANCEMENT_ID, equipEnhancementId);
      oneRow.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS__EQUIP_ID, equipId);
      oneRow.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS__EQUIP_LEVEL, equipLevel);
      oneRow.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS__ENHANCEMENT_PERCENTAGE_BEFORE_ENHANCEMENT, 
          enhancementPercentageBeforeEnhancement);
      
      newRows.add(oneRow);
    }
    List<Integer> feederIds = DBConnection.get().insertIntoTableBasicReturnIds(tableName, newRows);
    return feederIds;
  }
  
  public int insertIntoEquipEnhancementFeedersHistory(int id, int equipEnhancementId,
      int equipId, int equipLevel, int enhancementPercentageBeforeEnhancement) {

    String tableName = DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS_HISTORY;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__ID, id);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__EQUIP_ENHANCEMENT_ID, equipEnhancementId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__EQUIP_ID, equipId);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_HISTORY__EQUIP_LEVEL, equipLevel);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__ENHANCEMENT_PERCENTAGE,
        enhancementPercentageBeforeEnhancement);
    
    int numInserted = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    return numInserted;
  }
  
  public int insertMultipleIntoEquipEnhancementFeedersHistory(int equipEnhancementId, List<EquipEnhancementFeeder> feeders) {
    String tablename = DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS_HISTORY;
    int amount = feeders.size();
    List<Object> equipEnhancementFeedersIds = new ArrayList<Object>(amount);
    List<Object> equipEnhancementIds = new ArrayList<Object>(Collections.nCopies(amount, equipEnhancementId));
    List<Object> equipIds = new ArrayList<Object>(amount);
    List<Object> equipLevels = new ArrayList<Object>();
    List<Object> enhancementPercentages = new ArrayList<Object>();
    
    for(EquipEnhancementFeeder aFeeder : feeders) {
      equipEnhancementFeedersIds.add(aFeeder.getId());
      equipIds.add(aFeeder.getEquipId());
      equipLevels.add(aFeeder.getEquipLevel());
      enhancementPercentages.add(aFeeder.getEnhancementPercentageBeforeEnhancement());
    }
    Map<String, List<Object>> insertParams = new HashMap<String, List<Object>>();
    
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__ID, equipEnhancementFeedersIds);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__EQUIP_ENHANCEMENT_ID, equipEnhancementIds);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__EQUIP_ID, equipIds);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__EQUIP_LEVEL, equipLevels);
    insertParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS_HISTORY__ENHANCEMENT_PERCENTAGE,
        enhancementPercentages);
    
    int numInserted = DBConnection.get().insertIntoTableMultipleRows(tablename, insertParams, amount);
    return numInserted;
  }
  
  public List<Integer> insertUserEquips(int userId, List<Integer> equipIds, List<Integer> levels,
      List<Integer> enhancement) {
	  String tableName = DBConstants.TABLE_USER_EQUIP;
	  List<Map<String, Object>> newRows = new ArrayList<Map<String, Object>>();
	  for(int i = 0; i < equipIds.size(); i++){
		  Map<String, Object> row = new HashMap<String, Object>();
		  row.put(DBConstants.USER_EQUIP__USER_ID, userId);
		  row.put(DBConstants.USER_EQUIP__EQUIP_ID, equipIds.get(i));
		  row.put(DBConstants.USER_EQUIP__LEVEL, levels.get(i));
		  if (null == enhancement || enhancement.isEmpty()) {
		    row.put(DBConstants.USER_EQUIP__ENHANCEMENT_PERCENT, enhancement.get(i));
		  }
		  newRows.add(row);
	  }
	  List<Integer> userEquipIds = DBConnection.get().insertIntoTableBasicReturnIds(tableName, newRows);
	  Log.info("userEquipIds= " + userEquipIds);
	  return userEquipIds;
  }

  public int insertForgeAttemptIntoBlacksmith(int userId, int equipId,
      int goalLevel, boolean paidToGuarantee, Timestamp startTime,
      int diamondCostForGuarantee, Timestamp timeOfSpeedup, boolean attemptComplete,
      int enhancementPercentOne, int enhancementPercentTwo) {
    Map<String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.BLACKSMITH__USER_ID, userId);
    insertParams.put(DBConstants.BLACKSMITH__EQUIP_ID, equipId);
    insertParams.put(DBConstants.BLACKSMITH__GOAL_LEVEL, goalLevel);
    insertParams.put(DBConstants.BLACKSMITH__GUARANTEED, paidToGuarantee);
    insertParams.put(DBConstants.BLACKSMITH__START_TIME, startTime);
    insertParams.put(DBConstants.BLACKSMITH__ATTEMPT_COMPLETE, attemptComplete);
    insertParams.put(DBConstants.BLACKSMITH__EQUIP_ONE_ENHANCEMENT_PERCENT,
        enhancementPercentOne);
    insertParams.put(DBConstants.BLACKSMITH__EQUIP_TWO_ENHANCEMENT_PERCENT,
        enhancementPercentTwo);
    if (diamondCostForGuarantee > 0) {
      insertParams.put(DBConstants.BLACKSMITH__DIAMOND_GUARANTEE_COST, diamondCostForGuarantee);
    }

    if (timeOfSpeedup != null) {
      insertParams.put(DBConstants.BLACKSMITH__TIME_OF_SPEEDUP, timeOfSpeedup);
    }

    int blacksmithAttemptId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_BLACKSMITH, insertParams);
    return blacksmithAttemptId;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertAdcolonyRecentHistory(int, java.sql.Timestamp, int, java.lang.String)
   */
  @Override
  public boolean insertAdcolonyRecentHistory(int userId,
      Timestamp timeOfReward, int amountEarned, AdColonyRewardType adColonyRewardType, String digest) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__TIME_OF_REWARD,
        timeOfReward);
    if (adColonyRewardType==AdColonyRewardType.DIAMONDS) {
      insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIAMONDS_EARNED,
          amountEarned);
    } else if (adColonyRewardType==AdColonyRewardType.COINS) {
      insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__COINS_EARNED,
          amountEarned);
    }
    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIGEST, digest);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_ADCOLONY_RECENT_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertBattleHistory(int, int,
   * com.lvl6.proto.InfoProto.BattleResult, java.util.Date, int, int, int)
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertBattleHistory(int, int, com.lvl6.proto.InfoProto.BattleResult, java.util.Date, int, int, int)
   */
  @Override
  public boolean insertBattleHistory(int attackerId, int defenderId,
      BattleResult result, Date battleCompleteTime, int coinsStolen,
      int stolenEquipId, int expGained, int stolenEquipLevel) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.BATTLE_HISTORY__ATTACKER_ID, attackerId);
    insertParams.put(DBConstants.BATTLE_HISTORY__DEFENDER_ID, defenderId);
    insertParams
    .put(DBConstants.BATTLE_HISTORY__RESULT, result.getNumber());
    insertParams.put(DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME,
        battleCompleteTime);
    if (coinsStolen > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__COINS_STOLEN,
          coinsStolen);
    }
    if (stolenEquipId > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__EQUIP_STOLEN,
          stolenEquipId);
    }
    if (stolenEquipLevel > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__STOLEN_EQUIP_LEVEL,
          stolenEquipLevel);
    }
    if (expGained > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__EXP_GAINED, expGained);
    }

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_BATTLE_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertUnredeemedUserQuest(int, int, boolean, boolean)
   */
  @Override
  /*@Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})*/
  public boolean insertUnredeemedUserQuest(int userId, int questId,
      boolean hasNoRequiredTasks, boolean hasNoRequiredDefeatTypeJobs) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    insertParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE,
        hasNoRequiredTasks);
    insertParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE,
        hasNoRequiredDefeatTypeJobs);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_QUESTS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* used for quest defeat type jobs */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertCompletedDefeatTypeJobIdForUserQuest(int, int, int)
   */
  @Override
  //@CacheEvict(value="questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
  public boolean insertCompletedDefeatTypeJobIdForUserQuest(int userId,
      int dtjId, int questId) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(
        DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID,
        userId);
    insertParams.put(
        DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID,
        questId);
    insertParams
    .put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID,
        dtjId);

    int numInserted = DBConnection.get().insertIntoTableIgnore(
        DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS,
        insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* used for quest tasks */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertCompletedTaskIdForUserQuest(int, int, int)
   */
  @Override
  //@CacheEvict(value = "questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
  public boolean insertCompletedTaskIdForUserQuest(int userId, int taskId,
      int questId) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID,
        userId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID,
        questId);
    insertParams.put(
        DBConstants.USER_QUESTS_COMPLETED_TASKS__COMPLETED_TASK_ID,
        taskId);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertUserStructJustBuilt(int, int, java.sql.Timestamp, java.sql.Timestamp, com.lvl6.info.CoordinatePair)
   */
  @Override
  public boolean insertUserStructJustBuilt(int userId, int structId,
      Timestamp timeOfStructPurchase, Timestamp timeOfStructBuild,
      CoordinatePair structCoords) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_STRUCTS__STRUCT_ID, structId);
    insertParams
    .put(DBConstants.USER_STRUCTS__X_COORD, structCoords.getX());
    insertParams
    .put(DBConstants.USER_STRUCTS__Y_COORD, structCoords.getY());
    insertParams.put(DBConstants.USER_STRUCTS__PURCHASE_TIME,
        timeOfStructPurchase);
    insertParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED,
        timeOfStructBuild);
    insertParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, true);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_STRUCTS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /*
   * returns the id of the userstruct, -1 if none
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertUserStruct(int, int, com.lvl6.info.CoordinatePair, java.sql.Timestamp)
   */
  @Override
  public int insertUserStruct(int userId, int structId,
      CoordinatePair coordinates, Timestamp timeOfPurchase) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_STRUCTS__STRUCT_ID, structId);
    insertParams.put(DBConstants.USER_STRUCTS__X_COORD, coordinates.getX());
    insertParams.put(DBConstants.USER_STRUCTS__Y_COORD, coordinates.getY());
    insertParams.put(DBConstants.USER_STRUCTS__PURCHASE_TIME,
        timeOfPurchase);

    int userStructId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_USER_STRUCTS, insertParams);
    return userStructId;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertIAPHistoryElem(org.json.JSONObject, int, com.lvl6.info.User, double)
   */
  @Override
  public boolean insertIAPHistoryElem(JSONObject appleReceipt,
      int diamondChange, int coinChange, User user, double cashCost) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    try {
      insertParams.put(DBConstants.IAP_HISTORY__USER_ID, user.getId());
      insertParams.put(DBConstants.IAP_HISTORY__TRANSACTION_ID,
          appleReceipt.getString(IAPValues.TRANSACTION_ID));
      insertParams.put(DBConstants.IAP_HISTORY__PURCHASE_DATE,
          new Timestamp(appleReceipt.getLong(IAPValues.PURCHASE_DATE_MS)));
      insertParams.put(DBConstants.IAP_HISTORY__PREMIUMCUR_PURCHASED,
          diamondChange);
      insertParams.put(DBConstants.IAP_HISTORY__REGCUR_PURCHASED,
          coinChange);
      insertParams.put(DBConstants.IAP_HISTORY__CASH_SPENT, cashCost);
      insertParams.put(DBConstants.IAP_HISTORY__UDID, user.getUdid());
      insertParams.put(DBConstants.IAP_HISTORY__PRODUCT_ID,
          appleReceipt.getString(IAPValues.PRODUCT_ID));
      insertParams.put(DBConstants.IAP_HISTORY__QUANTITY,
          appleReceipt.getString(IAPValues.QUANTITY));
      insertParams.put(DBConstants.IAP_HISTORY__BID,
          appleReceipt.getString(IAPValues.BID));
      insertParams.put(DBConstants.IAP_HISTORY__BVRS,
          appleReceipt.getString(IAPValues.BVRS));

      if (appleReceipt.has(IAPValues.APP_ITEM_ID)) {
        insertParams.put(DBConstants.IAP_HISTORY__APP_ITEM_ID,
            appleReceipt.getString(IAPValues.APP_ITEM_ID));
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_IAP_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertMarketplaceItem(int, com.lvl6.proto.InfoProto.MarketplacePostType, int, int, int, java.sql.Timestamp)
   */
  @Override
  public boolean insertMarketplaceItem(int posterId,
      MarketplacePostType postType, int postedEquipId, int diamondCost,
      int coinCost, Timestamp timeOfPost, int equipLevel,
      int equipEnhancementPercent) {
    Map<String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    insertParams.put(DBConstants.MARKETPLACE__POST_TYPE,
        postType.getNumber());
    insertParams.put(DBConstants.MARKETPLACE__POSTED_EQUIP_ID,
        postedEquipId);
    insertParams.put(DBConstants.MARKETPLACE__TIME_OF_POST, timeOfPost);
    insertParams.put(DBConstants.MARKETPLACE__EQUIP_LEVEL, equipLevel);

    if (diamondCost > 0) {
      insertParams
      .put(DBConstants.MARKETPLACE__DIAMOND_COST, diamondCost);
    }
    if (coinCost > 0) {
      insertParams.put(DBConstants.MARKETPLACE__COIN_COST, coinCost);
    }
    if(equipEnhancementPercent > 0) {
      insertParams.put(
          DBConstants.MARKETPLACE__EQUIP_ENHANCEMENT_PERCENT,
          equipEnhancementPercent);
    }

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_MARKETPLACE, insertParams);
    if (numInserted == 1) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertMarketplaceItemIntoHistory(com.lvl6.info.MarketplacePost, int)
   */
  @Override
  public boolean insertMarketplaceItemIntoHistory(MarketplacePost mp,
      int buyerId, boolean sellerHasLicense) {
    Map<String, Object> insertParams = new HashMap<String, Object>();

    MarketplacePostType postType = mp.getPostType();

    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__MARKETPLACE_ID,
        mp.getId());
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID,
        mp.getPosterId());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__BUYER_ID,
        buyerId);
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POST_TYPE,
        postType.getNumber());
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_POST,
        mp.getTimeOfPost());
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_PURCHASE,
        new Timestamp(new Date().getTime()));
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__EQUIP_LEVEL, 
        mp.getEquipLevel());
    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__SELLER_HAS_LICENSE, 
        sellerHasLicense);

    insertParams.put(
        DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_ID,
        mp.getPostedEquipId());
    if (mp.getDiamondCost() > 0) {
      insertParams.put(
          DBConstants.MARKETPLACE_TRANSACTION_HISTORY__DIAMOND_COST,
          mp.getDiamondCost());
    }
    if (mp.getCoinCost() > 0) {
      insertParams.put(
          DBConstants.MARKETPLACE_TRANSACTION_HISTORY__COIN_COST,
          mp.getCoinCost());
    }
    
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__EQUIP_ENHANCEMENT_PERCENT,
        mp.getEquipEnhancementPercentage());

    int numInserted = DBConnection.get()
        .insertIntoTableBasic(
            DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY,
            insertParams);
    if (numInserted == 1) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertReferral(int, int, int)
   */
  @Override
  public boolean insertReferral(int referrerId, int referredId,
      int coinsGivenToReferrer) {
    Map<String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.REFERRALS__REFERRER_ID, referrerId);
    insertParams.put(DBConstants.REFERRALS__NEWLY_REFERRED_ID, referredId);
    insertParams.put(DBConstants.REFERRALS__TIME_OF_REFERRAL,
        new Timestamp(new Date().getTime()));
    insertParams.put(DBConstants.REFERRALS__COINS_GIVEN_TO_REFERRER,
        new Timestamp(new Date().getTime()));

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_REFERRALS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  // returns -1 if error
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertUser(java.lang.String, java.lang.String, com.lvl6.proto.InfoProto.UserType, com.lvl6.info.Location, java.lang.String, java.lang.String, int, int, int, int, int, int, int, int, int, java.lang.Integer, java.lang.Integer, java.lang.Integer, boolean)
   */
  @Override
  public int insertUser(String udid, String name, UserType type,
      Location location, String deviceToken, String newReferCode,
      int level, int attack, int defense, int energy,
      int stamina, int experience, int coins, int diamonds,
      Integer weaponEquipped, Integer armorEquipped,
      Integer amuletEquipped, boolean isFake, int numGroupChatsRemaining) {

    Timestamp now = new Timestamp(new Date().getTime());
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER__UDID, udid);
    insertParams.put(DBConstants.USER__NAME, name);
    insertParams.put(DBConstants.USER__TYPE, type.getNumber());
    insertParams.put(DBConstants.USER__LEVEL, level);
    insertParams.put(DBConstants.USER__ATTACK, attack);
    insertParams.put(DBConstants.USER__DEFENSE, defense);
    insertParams.put(DBConstants.USER__ENERGY, energy);
    insertParams.put(DBConstants.USER__ENERGY_MAX, energy);
    insertParams.put(DBConstants.USER__STAMINA, stamina);
    insertParams.put(DBConstants.USER__STAMINA_MAX, stamina);
    insertParams.put(DBConstants.USER__EXPERIENCE, experience);
    insertParams.put(DBConstants.USER__COINS, coins);
    insertParams.put(DBConstants.USER__DIAMONDS, diamonds);
    insertParams.put(DBConstants.USER__REFERRAL_CODE, newReferCode);
    insertParams.put(DBConstants.USER__LATITUDE, location.getLatitude());
    insertParams.put(DBConstants.USER__LONGITUDE, location.getLongitude());
    insertParams.put(DBConstants.USER__LAST_LOGIN, now);
    insertParams.put(DBConstants.USER__DEVICE_TOKEN, deviceToken);
    insertParams.put(DBConstants.USER__IS_FAKE, isFake);
    insertParams.put(DBConstants.USER__WEAPON_EQUIPPED_USER_EQUIP_ID,
        weaponEquipped);
    insertParams.put(DBConstants.USER__ARMOR_EQUIPPED_USER_EQUIP_ID,
        armorEquipped);
    insertParams.put(DBConstants.USER__AMULET_EQUIPPED_USER_EQUIP_ID,
        amuletEquipped);
    insertParams.put(DBConstants.USER__CREATE_TIME, now);
    insertParams.put(DBConstants.USER__NUM_GROUP_CHATS_REMAINING, 5);

    int userId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_USER, insertParams);
    return userId;
  }

  /*
   * returns the id of the post, -1 if none
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertPlayerWallPost(int, int, java.lang.String, java.sql.Timestamp)
   */
  @Override
  public int insertPlayerWallPost(int posterId, int wallOwnerId,
      String content, Timestamp timeOfPost) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__POSTER_ID, posterId);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__WALL_OWNER_ID,
        wallOwnerId);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__TIME_OF_POST,
        timeOfPost);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__CONTENT, content);

    int wallPostId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_PLAYER_WALL_POSTS, insertParams);
    return wallPostId;
  }

  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertKiipHistory(int, java.sql.Timestamp, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public boolean insertKiipHistory(int userId, Timestamp clientTime,
      String content, String signature, int quantity, String transactionId) {

    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__CONTENT, content);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__QUANTITY, quantity);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__SIGNATURE, signature);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__TIME_OF_REWARD,
        clientTime);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__TRANSACTION_ID,
        transactionId);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.KIIP_REWARD_HISTORY__SIGNATURE, signature);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_KIIP_REWARD_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  @Override
  public int insertIddictionIndentifier(String identifier, Date clickTime) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.IDDICTION_IDENTIFIERS__IDENTIFIER, identifier);
    insertParams.put(DBConstants.IDDICTION_IDENTIFIERS__CLICK_TIME, new Timestamp(clickTime.getTime()));

    int iddictionIdentId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_IDDICTION_IDENTIFIERS, insertParams);
    return iddictionIdentId;
  }

  @Override
  public int insertClan(String name, int ownerId, Timestamp createTime, String description, String tag, boolean isGood,
      boolean requestToJoinRequired) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.CLANS__NAME, name);
    insertParams.put(DBConstants.CLANS__OWNER_ID, ownerId);
    insertParams.put(DBConstants.CLANS__CREATE_TIME, createTime);
    insertParams.put(DBConstants.CLANS__DESCRIPTION, description);
    insertParams.put(DBConstants.CLANS__TAG, tag);
    insertParams.put(DBConstants.CLANS__IS_GOOD, isGood);
    insertParams.put(DBConstants.CLANS__REQUEST_TO_JOIN_REQUIRED, requestToJoinRequired);

    int clanId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_CLANS, insertParams);
    return clanId;
  }

  @Override
  public boolean insertUserClan(int userId, int clanId, UserClanStatus status, Timestamp requestTime) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_CLANS__USER_ID, userId);
    insertParams.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    insertParams.put(DBConstants.USER_CLANS__STATUS, status.getNumber());
    insertParams.put(DBConstants.USER_CLANS__REQUEST_TIME, requestTime);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_CLANS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean insertDiamondEquipPurchaseHistory(int buyerId, int equipId, int diamondsSpent, Timestamp purchaseTime) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.DIAMOND_EQUIP_PURCHASE_HISTORY__BUYER_ID, buyerId);
    insertParams.put(DBConstants.DIAMOND_EQUIP_PURCHASE_HISTORY__DIAMONDS_SPENT, diamondsSpent);
    insertParams.put(DBConstants.DIAMOND_EQUIP_PURCHASE_HISTORY__EQUIP_ID, equipId);
    insertParams.put(DBConstants.DIAMOND_EQUIP_PURCHASE_HISTORY__PURCHASE_TIME, purchaseTime);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_DIAMOND_EQUIP_PURCHASE_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  @Override
  public int insertClanBulletinPost(int userId, int clanId, String content,
      Timestamp timeOfPost) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.CLAN_BULLETIN_POSTS__POSTER_ID, userId);
    insertParams.put(DBConstants.CLAN_BULLETIN_POSTS__CLAN_ID,
        clanId);
    insertParams.put(DBConstants.CLAN_BULLETIN_POSTS__TIME_OF_POST,
        timeOfPost);
    insertParams.put(DBConstants.CLAN_BULLETIN_POSTS__CONTENT, content);

    int wallPostId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_CLAN_BULLETIN_POSTS, insertParams);
    return wallPostId;
  }

  @Override
  public int insertClanChatPost(int userId, int clanId, String content,
      Timestamp timeOfPost) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.CLAN_WALL_POSTS__POSTER_ID, userId);
    insertParams.put(DBConstants.CLAN_WALL_POSTS__CLAN_ID,
        clanId);
    insertParams.put(DBConstants.CLAN_WALL_POSTS__TIME_OF_POST,
        timeOfPost);
    insertParams.put(DBConstants.CLAN_WALL_POSTS__CONTENT, content);

    int wallPostId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_CLAN_WALL_POSTS, insertParams);
    return wallPostId;
  }
  
  public int insertIntoBossRewardDropHistoryReturnId(int bossId, int userId, int silverDropped, int goldDropped, Timestamp timeOfDrop) {
	  String tableName = DBConstants.TABLE_BOSS_REWARD_DROP_HISTORY;
	  Map<String, Object> insertParams = new HashMap<String, Object>();
	  
	  insertParams.put(DBConstants.BOSS_REWARD_DROP_HISTORY__BOSS_ID, bossId);
	  insertParams.put(DBConstants.BOSS_REWARD_DROP_HISTORY__USER_ID, userId);
	  insertParams.put(DBConstants.BOSS_REWARD_DROP_HISTORY__SILVER, silverDropped);
	  insertParams.put(DBConstants.BOSS_REWARD_DROP_HISTORY__GOLD, goldDropped);
	  insertParams.put(DBConstants.BOSS_REWARD_DROP_HISTORY__TIME_OF_DROP, timeOfDrop);
	  
	  int anId = DBConnection.get().insertIntoTableBasicReturnId(tableName, insertParams);
	  return anId;
  }
  
  public int insertIntoBossEquipDropHistory(int bossRewardDropHistoryId, List<Integer> equipIds) {
	  String tableName = DBConstants.TABLE_BOSS_EQUIP_DROP_HISTORY;
	  Map<String, List<Object>> insertParams = new HashMap<String, List<Object>>();
	  
	  Map<Integer, Integer> equipIdsAndQuantities = generateEquipIdAndQuantitiesMap(equipIds);
	  
	  List<Object> bossRewardDropHistoryIds = new ArrayList<Object>();
	  List<Object> distinctEquipIds = new ArrayList<Object>();
	  
	  for(Integer distinctEquipId: equipIdsAndQuantities.keySet()) {
	    bossRewardDropHistoryIds.add(bossRewardDropHistoryId);
	    distinctEquipIds.add(distinctEquipId);
	  }
	  
	  List<Object> quantities = new ArrayList<Object>();
	  for(Object equipId : distinctEquipIds) {
	    quantities.add(equipIdsAndQuantities.get((Integer) equipId));
	  }
	  
	  int numRows = bossRewardDropHistoryIds.size();
	  
	  insertParams.put(DBConstants.BOSS_EQUIP_DROP_HISTORY__BOSS_REWARD_DROP_HISTORY_ID, bossRewardDropHistoryIds);
	  insertParams.put(DBConstants.BOSS_EQUIP_DROP_HISTORY__EQUIP_ID, distinctEquipIds);
	  insertParams.put(DBConstants.BOSS_EQUIP_DROP_HISTORY__QUANTITY, quantities);
	  
	  int numUpdated = DBConnection.get().insertIntoTableMultipleRows(tableName, insertParams, numRows);
	  return numUpdated;
	  
  }
  
  private Map<Integer, Integer> generateEquipIdAndQuantitiesMap(List<Integer> equipIds) {
    Map<Integer, Integer> equipIdsAndQuantities = new HashMap<Integer, Integer>();
    for(Integer equipId: equipIds) {
      Integer quantity = equipIdsAndQuantities.get(equipId);
      
      if(null == quantity) {
        equipIdsAndQuantities.put(equipId, 0);
        quantity = 0;
      }
      equipIdsAndQuantities.put(equipId, quantity+1);
    }
    return equipIdsAndQuantities;
  }
  
  public int insertIntoUserLeaderboardEvent(int leaderboardEventId, int userId, 
      int battlesWonChange, int battlesLostChange, int battlesFledChange) {
    String tablename = DBConstants.TABLE_USER_LEADERBOARD_EVENTS;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    Map<String, Object> relativeUpdates = new HashMap<String, Object>();
    Map<String, Object> absoluteUpdates = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.USER_LEADERBOARD_EVENTS__LEADERBOARD_EVENT_ID, leaderboardEventId);
    insertParams.put(DBConstants.USER_LEADERBOARD_EVENTS__USER_ID, userId);
    //as long as there is an existing row, setting these three values doesn't matter
    //this is here just for the initial insert
    insertParams.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_WON, battlesWonChange);
    insertParams.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_LOST, battlesLostChange);
    insertParams.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_FLED, battlesFledChange);
    
    //this is for the case when there is already an existing row
    relativeUpdates.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_WON, battlesWonChange);
    relativeUpdates.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_LOST, battlesLostChange);
    relativeUpdates.put(DBConstants.USER_LEADERBOARD_EVENTS__BATTLES_FLED, battlesFledChange);
    DBConnection.get().insertOnDuplicateKeyUpdate(tablename, insertParams, relativeUpdates, absoluteUpdates);
    return 0;
  }
  
  //at the time of this writing 12/19/12 only used to record when user spends diamonds to
  //refill energy or stamina
  public int insertIntoRefillStatHistory(int userId, boolean staminaRefill,
      int staminaMax, int goldCost) {
    String tablename = DBConstants.TABLE_REFILL_STAT_HISTORY;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.REFILL_STAT_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.REFILL_STAT_HISTORY__STAMINA_REFILL, staminaRefill);
    insertParams.put(DBConstants.REFILL_STAT_HISTORY__STAMINA_MAX, staminaMax);
    insertParams.put(DBConstants.REFILL_STAT_HISTORY__GOLD_COST, goldCost);
    
    //number of rows inserted (should be one)
    int numUpdated =  DBConnection.get().insertIntoTableBasic(tablename, insertParams);
    Log.info("number of rows inserted into refill_stat_history table: " + numUpdated);
    return numUpdated;
  }
  
  //0 for isSilver means currency is gold; 1 for isSilver means currency is silver
  public int insertIntoUserCurrencyHistory (int userId, Timestamp date, int isSilver, 
      int currencyChange, int currencyBefore, int currencyAfter, String reasonForChange) {
    String tableName = DBConstants.TABLE_USER_CURRENCY_HISTORY;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__DATE, date);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__IS_SILVER, isSilver);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_CHANGE, currencyChange);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_BEFORE_CHANGE, currencyBefore);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_AFTER_CHANGE, currencyAfter);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__REASON_FOR_CHANGE, reasonForChange);
    
    //number of rows inserted
    int numUpdated = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    Log.info("number of rows inserted into user_currency_history: " + numUpdated);
    return numUpdated;
  }
  
  /*
   * assumptions: all the entries at index i across all the lists, 
   * they make up the values for one row to insert into user_currency_history
   */
  @SuppressWarnings("unchecked") //the generics issue noted below
  public int insertIntoUserCurrencyHistoryMultipleRows(List<Integer> userIds, List<Timestamp> dates, 
      List<Integer> areSilver, List<Integer> changesToCurrencies, List<Integer> previousCurrencies, 
      List<Integer> currentCurrencies, List<String> reasonsForChanges) {
    String tablename = DBConstants.TABLE_USER_CURRENCY_HISTORY;
    
    //did not add generics because eclipse shows errors like: can't accept  (String, List<Integer>), needs (String, List<Object>)
    @SuppressWarnings("rawtypes")
    Map insertParams = new HashMap<String, List<Object>>();
    int numRows = userIds.size();

    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__USER_ID,
        userIds);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__DATE, dates);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__IS_SILVER, areSilver);
    if(null != changesToCurrencies && 0 < changesToCurrencies.size()) {
      insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_CHANGE, changesToCurrencies);
    }
    if(null != previousCurrencies && 0 < previousCurrencies.size()) {
      insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_BEFORE_CHANGE, previousCurrencies);
    }
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__CURRENCY_AFTER_CHANGE, currentCurrencies);
    insertParams.put(DBConstants.USER_CURRENCY_HISTORY__REASON_FOR_CHANGE, reasonsForChanges);
    
    int numInserted = DBConnection.get().insertIntoTableMultipleRows(tablename, 
        insertParams, numRows);
    
    return numInserted;
  }
  
  public int insertIntoLoginHistory(String udid, int userId, Timestamp now, boolean isLogin,
      boolean goingThroughTutorial) {
    String tableName = DBConstants.TABLE_LOGIN_HISTORY;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.LOGIN_HISTORY__UDID, udid);
    //if going through tutorial, no id exists
    if(!goingThroughTutorial) {
      insertParams.put(DBConstants.LOGIN_HISTORY__USER_ID, userId);
    }
    insertParams.put(DBConstants.LOGIN_HISTORY__DATE, now);
    insertParams.put(DBConstants.LOGIN_HISTORY__IS_LOGIN, isLogin);
    
    int numInserted = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    
    return numInserted;
  }
  
  public int insertIntoFirstTimeUsers(String openUdid, String udid, String mac, String advertiserId,
      Timestamp now) {
    String tableName = DBConstants.TABLE_FIRST_TIME_USERS;
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.FIRST_TIME_USERS__OPEN_UDID, openUdid);
    insertParams.put(DBConstants.FIRST_TIME_USERS__UDID, udid);
    insertParams.put(DBConstants.FIRST_TIME_USERS__MAC, mac);
    insertParams.put(DBConstants.FIRST_TIME_USERS__ADVERTISER_ID, advertiserId);
    insertParams.put(DBConstants.FIRST_TIME_USERS__CREATE_TIME, now);
    
    int numInserted = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    
    return numInserted;
  }
  
  public int insertIntoUserBoosterPackHistory(int userId, int boosterPackId, 
      int numBought, Timestamp timeOfPurchase, int rarityOneQuantity, 
      int rarityTwoQuantity, int rarityThreeQuantity) {
    String tableName = DBConstants.TABLE_USER_BOOSTER_PACK_HISTORY;
    
    Map<String, Object> insertParams = new HashMap<String, Object>();
    
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__USER_ID, userId);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__BOOSTER_PACK_ID, boosterPackId);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__NUM_BOUGHT, numBought);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__TIME_OF_PURCHASE, timeOfPurchase);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__RARITY_ONE_QUANTITY, rarityOneQuantity);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__RARITY_TWO_QUANTITY, rarityTwoQuantity);
    insertParams.put(DBConstants.USER_BOOSTER_PACK_HISTORY__RARITY_THREE_QUANTITY, rarityThreeQuantity);
    
    int numInserted = DBConnection.get().insertIntoTableBasic(tableName, insertParams);
    return numInserted;
  }
}
