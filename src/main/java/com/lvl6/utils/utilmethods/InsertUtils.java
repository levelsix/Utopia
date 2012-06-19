package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.DBConstants;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;

public class InsertUtils implements InsertUtil {
	
	
  
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertUserEquips(int, java.util.List, int)
 */
@Override
public boolean insertUserEquips(int userId, List<Integer> equipIds, int quantity) {
      //insertIntoTableMultipleRows(String tablename, Map<String, List<Object>> insertParams, int numRows) {
    
    Map<String, List<Object>> insertParams = new HashMap<String, List<Object>>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, new ArrayList<Object>());
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, new ArrayList<Object>());
    insertParams.put(DBConstants.USER_EQUIP__QUANTITY, new ArrayList<Object>());
    for (Integer equipId : equipIds) {
      insertParams.get(DBConstants.USER_EQUIP__USER_ID).add(userId);
      insertParams.get(DBConstants.USER_EQUIP__EQUIP_ID).add(equipId);
      insertParams.get(DBConstants.USER_EQUIP__QUANTITY).add(quantity);
    }
    
    int numInserted = DBConnection.get().insertIntoTableMultipleRows(DBConstants.TABLE_USER_EQUIP, insertParams, equipIds.size());
    if (numInserted == equipIds.size()) {
      return true;
    }
    return false;
  }
  
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertAviaryAndCarpenterCoords(int, com.lvl6.info.CoordinatePair, com.lvl6.info.CoordinatePair)
 */
@Override
public boolean insertAviaryAndCarpenterCoords(int userId, CoordinatePair aviary, CoordinatePair carpenter) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);
    insertParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_X_COORD, aviary.getX());
    insertParams.put(DBConstants.USER_CITY_ELEMS__AVIARY_Y_COORD, aviary.getY());
    insertParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_X_COORD, carpenter.getX());
    insertParams.put(DBConstants.USER_CITY_ELEMS__CARPENTER_Y_COORD, carpenter.getY());

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_USER_CITY_ELEMS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
  


public boolean insertApsalarRecentHistory(int userId, Timestamp timeOfReward, int diamondsEarned, 
	      String digest) {
	    Map <String, Object> insertParams = new HashMap<String, Object>();
	    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__USER_ID, userId);
	    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__TIME_OF_REWARD, timeOfReward);
	    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIAMONDS_EARNED, diamondsEarned);
	    insertParams.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIGEST, digest);
	    
	    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_ADCOLONY_RECENT_HISTORY, insertParams);
	    if (numInserted == 1) {
	      return true;
	    }
	    return false;
	  }
	  

  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertBattleHistory(int, int, com.lvl6.proto.InfoProto.BattleResult, java.util.Date, int, int, int)
 */
@Override
public boolean insertBattleHistory(int attackerId, int defenderId, BattleResult result, 
      Date battleCompleteTime, int coinsStolen, int stolenEquipId, int expGained) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.BATTLE_HISTORY__ATTACKER_ID, attackerId);
    insertParams.put(DBConstants.BATTLE_HISTORY__DEFENDER_ID, defenderId);
    insertParams.put(DBConstants.BATTLE_HISTORY__RESULT, result.getNumber());
    insertParams.put(DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, battleCompleteTime);
    if (coinsStolen > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__COINS_STOLEN, coinsStolen);
    }
    if (stolenEquipId > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__EQUIP_STOLEN, stolenEquipId); 
    }
    if (expGained > 0) {
      insertParams.put(DBConstants.BATTLE_HISTORY__EXP_GAINED, expGained);
    }
    
    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_BATTLE_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
  
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertUnredeemedUserQuest(int, int, boolean, boolean)
 */
@Caching(evict={@CacheEvict(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId"),
		@CacheEvict(value="incompleteUserQuestsForUser", key="#userId"),
		@CacheEvict(value="unredeemedUserQuestsForUser", key="#userId")})
@Override
public boolean insertUnredeemedUserQuest(int userId, int questId, boolean hasNoRequiredTasks, boolean hasNoRequiredDefeatTypeJobs) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    insertParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, hasNoRequiredTasks);
    insertParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, hasNoRequiredDefeatTypeJobs);

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
  
  /* used for quest defeat type jobs*/
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertCompletedDefeatTypeJobIdForUserQuest(int, int, int)
 */
@Override
public boolean insertCompletedDefeatTypeJobIdForUserQuest(int userId, int dtjId, int questId) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID, dtjId);

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* used for quest tasks*/
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertCompletedTaskIdForUserQuest(int, int, int)
 */
@Override
public boolean insertCompletedTaskIdForUserQuest(int userId, int taskId, int questId) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__COMPLETED_TASK_ID, taskId);

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }


  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertUserStructJustBuilt(int, int, java.sql.Timestamp, java.sql.Timestamp, com.lvl6.info.CoordinatePair)
 */
@Override
@Caching(evict= {@CacheEvict(value="structIdsToUserStructsForUser", key="#userId"),
		  @CacheEvict(value="structIdsToUserStructsForUser", key="#userId")})
public boolean insertUserStructJustBuilt(int userId, int structId, Timestamp timeOfStructPurchase,
      Timestamp timeOfStructBuild, CoordinatePair structCoords) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_STRUCTS__STRUCT_ID, structId);
    insertParams.put(DBConstants.USER_STRUCTS__X_COORD, structCoords.getX());
    insertParams.put(DBConstants.USER_STRUCTS__Y_COORD, structCoords.getY());
    insertParams.put(DBConstants.USER_STRUCTS__PURCHASE_TIME, timeOfStructPurchase);
    insertParams.put(DBConstants.USER_STRUCTS__LAST_RETRIEVED, timeOfStructBuild);
    insertParams.put(DBConstants.USER_STRUCTS__IS_COMPLETE, true);

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_USER_STRUCTS, insertParams);
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
@Caching(evict= {@CacheEvict(value="structIdsToUserStructsForUser", key="#userId"),
		  @CacheEvict(value="structIdsToUserStructsForUser", key="#userId")})
public int insertUserStruct(int userId, int structId, CoordinatePair coordinates, Timestamp timeOfPurchase) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_STRUCTS__STRUCT_ID, structId);
    insertParams.put(DBConstants.USER_STRUCTS__X_COORD, coordinates.getX());
    insertParams.put(DBConstants.USER_STRUCTS__Y_COORD, coordinates.getY());
    insertParams.put(DBConstants.USER_STRUCTS__PURCHASE_TIME, timeOfPurchase);

    int userStructId = DBConnection.get().insertIntoTableBasicReturnId(DBConstants.TABLE_USER_STRUCTS, insertParams);
    return userStructId;
  }

  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertIAPHistoryElem(org.json.JSONObject, int, com.lvl6.info.User, double)
 */
@Override
public boolean insertIAPHistoryElem(JSONObject appleReceipt, int diamondChange, User user, double cashCost) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    try {
      insertParams.put(DBConstants.IAP_HISTORY__USER_ID, user.getId());
      insertParams.put(DBConstants.IAP_HISTORY__TRANSACTION_ID, appleReceipt.getString(IAPValues.TRANSACTION_ID));
      insertParams.put(DBConstants.IAP_HISTORY__PURCHASE_DATE, appleReceipt.getString(IAPValues.PURCHASE_DATE));
      insertParams.put(DBConstants.IAP_HISTORY__PREMIUMCUR_PURCHASED, diamondChange);
      insertParams.put(DBConstants.IAP_HISTORY__CASH_SPENT, cashCost);
      insertParams.put(DBConstants.IAP_HISTORY__UDID, user.getUdid());
      insertParams.put(DBConstants.IAP_HISTORY__PRODUCT_ID, appleReceipt.getString(IAPValues.PRODUCT_ID));
      insertParams.put(DBConstants.IAP_HISTORY__QUANTITY, appleReceipt.getString(IAPValues.QUANTITY));
      insertParams.put(DBConstants.IAP_HISTORY__BID, appleReceipt.getString(IAPValues.BID));
      insertParams.put(DBConstants.IAP_HISTORY__BVRS, appleReceipt.getString(IAPValues.BVRS));

      if (appleReceipt.has(IAPValues.APP_ITEM_ID)) {
        insertParams.put(DBConstants.IAP_HISTORY__APP_ITEM_ID, appleReceipt.getString(IAPValues.APP_ITEM_ID));
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_IAP_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertMarketplaceItem(int, com.lvl6.proto.InfoProto.MarketplacePostType, int, int, int, java.sql.Timestamp)
 */
@Override
public boolean insertMarketplaceItem(int posterId, MarketplacePostType postType, 
      int postedEquipId, int diamondCost, int coinCost, Timestamp timeOfPost) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    insertParams.put(DBConstants.MARKETPLACE__POST_TYPE, postType.getNumber());
    insertParams.put(DBConstants.MARKETPLACE__POSTED_EQUIP_ID, postedEquipId);
    insertParams.put(DBConstants.MARKETPLACE__TIME_OF_POST, timeOfPost);

    if (diamondCost > 0){
      insertParams.put(DBConstants.MARKETPLACE__DIAMOND_COST, diamondCost);
    }
    if (coinCost > 0) {
      insertParams.put(DBConstants.MARKETPLACE__COIN_COST, coinCost);
    }

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_MARKETPLACE, insertParams);
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
    Map <String, Object> insertParams = new HashMap<String, Object>();

    MarketplacePostType postType = mp.getPostType();

    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__MARKETPLACE_ID, mp.getId());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID, mp.getPosterId());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__BUYER_ID, buyerId);
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POST_TYPE, postType.getNumber());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_POST, mp.getTimeOfPost());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_PURCHASE, new Timestamp(new Date().getTime()));
    
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_ID, mp.getPostedEquipId());
    if (mp.getDiamondCost() > 0){
      insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__DIAMOND_COST, mp.getDiamondCost());
    }
    if (mp.getCoinCost() > 0) {
      insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__COIN_COST, mp.getCoinCost());
    }

    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertReferral(int, int, int)
 */
@Override
public boolean insertReferral(int referrerId, int referredId, int coinsGivenToReferrer) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.REFERRALS__REFERRER_ID, referrerId);
    insertParams.put(DBConstants.REFERRALS__NEWLY_REFERRED_ID, referredId);
    insertParams.put(DBConstants.REFERRALS__TIME_OF_REFERRAL, new Timestamp(new Date().getTime()));
    insertParams.put(DBConstants.REFERRALS__COINS_GIVEN_TO_REFERRER, new Timestamp(new Date().getTime()));
    
    int numInserted = DBConnection.get().insertIntoTableBasic(DBConstants.TABLE_REFERRALS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
  
  //returns -1 if error
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertUser(java.lang.String, java.lang.String, com.lvl6.proto.InfoProto.UserType, com.lvl6.info.Location, java.lang.String, java.lang.String, int, int, int, int, int, int, int, int, int, java.lang.Integer, java.lang.Integer, java.lang.Integer, boolean)
 */
@Override
public int insertUser(String udid, String name, UserType type, Location location, String deviceToken, String newReferCode, int level, 
      int attack, int defense, int energy, int health, int stamina, int experience, int coins, int diamonds, 
      Integer weaponEquipped, Integer armorEquipped, Integer amuletEquipped, boolean isFake) {
    
    Timestamp now = new Timestamp(new Date().getTime());
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER__UDID, udid);
    insertParams.put(DBConstants.USER__NAME, name);
    insertParams.put(DBConstants.USER__TYPE, type.getNumber());
    insertParams.put(DBConstants.USER__LEVEL, level);
    insertParams.put(DBConstants.USER__ATTACK, attack);
    insertParams.put(DBConstants.USER__DEFENSE, defense);
    insertParams.put(DBConstants.USER__ENERGY, energy);
    insertParams.put(DBConstants.USER__ENERGY_MAX, energy);
    insertParams.put(DBConstants.USER__HEALTH_MAX, health);
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
    insertParams.put(DBConstants.USER__WEAPON_EQUIPPED, weaponEquipped);
    insertParams.put(DBConstants.USER__ARMOR_EQUIPPED, armorEquipped);
    insertParams.put(DBConstants.USER__AMULET_EQUIPPED, amuletEquipped);
    insertParams.put(DBConstants.USER__CREATE_TIME, now);
    
    
    int userId = DBConnection.get().insertIntoTableBasicReturnId(DBConstants.TABLE_USER, insertParams);
    return userId;
  }

  
  /*
   * returns the id of the post, -1 if none
   */
  /* (non-Javadoc)
 * @see com.lvl6.utils.utilmethods.InsertUtil#insertPlayerWallPost(int, int, java.lang.String, java.sql.Timestamp)
 */
@Override
public int insertPlayerWallPost(int posterId, int wallOwnerId, String content, Timestamp timeOfPost) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__POSTER_ID, posterId);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__WALL_OWNER_ID, wallOwnerId);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__TIME_OF_POST, timeOfPost);
    insertParams.put(DBConstants.PLAYER_WALL_POSTS__CONTENT, content);

    int wallPostId = DBConnection.get().insertIntoTableBasicReturnId(DBConstants.TABLE_PLAYER_WALL_POSTS, insertParams);
    return wallPostId;
  }

  
}
