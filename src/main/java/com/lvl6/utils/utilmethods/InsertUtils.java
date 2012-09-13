package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
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
  @Caching(evict = {
      @CacheEvict(value = "userEquipsForUser", key = "#userId"),
      @CacheEvict(value = "equipsToUserEquipsForUser", key = "#userId"),
      @CacheEvict(value = "userEquipsWithEquipId", key = "#userId+':'+#equipId") })
  public int insertUserEquip(int userId, int equipId, int level) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, userId);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__LEVEL, level);

    int userEquipId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_USER_EQUIP, insertParams);
    return userEquipId;
  }

  public int insertForgeAttemptIntoBlacksmith(int userId, int equipId,
      int goalLevel, boolean paidToGuarantee, Timestamp startTime,
      int diamondCostForGuarantee, Timestamp timeOfSpeedup, boolean attemptComplete) {
    Map<String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.BLACKSMITH__USER_ID, userId);
    insertParams.put(DBConstants.BLACKSMITH__EQUIP_ID, equipId);
    insertParams.put(DBConstants.BLACKSMITH__GOAL_LEVEL, goalLevel);
    insertParams.put(DBConstants.BLACKSMITH__GUARANTEED, paidToGuarantee);
    insertParams.put(DBConstants.BLACKSMITH__START_TIME, startTime);
    insertParams.put(DBConstants.BLACKSMITH__ATTEMPT_COMPLETE, attemptComplete);

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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.lvl6.utils.utilmethods.InsertUtil#insertAviaryAndCarpenterCoords(int,
   * com.lvl6.info.CoordinatePair, com.lvl6.info.CoordinatePair)
   */
  /* (non-Javadoc)
   * @see com.lvl6.utils.utilmethods.InsertUtil#insertAviaryAndCarpenterCoords(int, com.lvl6.info.CoordinatePair, com.lvl6.info.CoordinatePair)
   */
  @Override
  public boolean insertAviaryAndCarpenterCoords(int userId,
      CoordinatePair aviary, CoordinatePair carpenter) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);
    insertParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_X_COORD,
        aviary.getX());
    insertParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_Y_COORD,
        aviary.getY());
    insertParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_X_COORD,
        carpenter.getX());
    insertParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_Y_COORD,
        carpenter.getY());

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_CITY_ELEMS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
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
  @Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
      @CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
      @CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
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
  @CacheEvict(value="questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
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
  @CacheEvict(value = "questIdToUserTasksCompletedForQuestForUserCache", key="#userId")
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
      int diamondChange, User user, double cashCost) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    try {
      insertParams.put(DBConstants.IAP_HISTORY__USER_ID, user.getId());
      insertParams.put(DBConstants.IAP_HISTORY__TRANSACTION_ID,
          appleReceipt.getString(IAPValues.TRANSACTION_ID));
      insertParams.put(DBConstants.IAP_HISTORY__PURCHASE_DATE,
          appleReceipt.getString(IAPValues.PURCHASE_DATE));
      insertParams.put(DBConstants.IAP_HISTORY__PREMIUMCUR_PURCHASED,
          diamondChange);
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
      int coinCost, Timestamp timeOfPost, int equipLevel) {
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
      int buyerId) {
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
  public int insertClan(String name, int ownerId, Timestamp createTime, String description, String tag) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.CLANS__NAME, name);
    insertParams.put(DBConstants.CLANS__OWNER_ID, ownerId);
    insertParams.put(DBConstants.CLANS__CREATE_TIME, createTime);
    insertParams.put(DBConstants.CLANS__DESCRIPTION, description);
    insertParams.put(DBConstants.CLANS__TAG, tag);

    int clanId = DBConnection.get().insertIntoTableBasicReturnId(
        DBConstants.TABLE_CLANS, insertParams);
    return clanId;
  }

  @Override
  public boolean insertUserClan(int userId, int clanId, UserClanStatus status) {
    Map<String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_CLANS__USER_ID, userId);
    insertParams.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    insertParams.put(DBConstants.USER_CLANS__STATUS, status);

    int numInserted = DBConnection.get().insertIntoTableBasic(
        DBConstants.TABLE_USER_CLANS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
}
