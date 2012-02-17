package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.DBConstants;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.utils.DBConnection;

public class InsertUtils {

  public static boolean createUser(String name) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.TABLE_USER, name);
    //TODO: IMPL- work out default stats. actually leave defaults in db so we dont need server reboot for change

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_USER, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  public static boolean insertUnredeemedUserQuest(int userId, int questId, boolean hasNoRequiredTasks, boolean hasNoRequiredDefeatTypeJobs) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    
    insertParams.put(DBConstants.USER_QUESTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS__TASKS_COMPLETE, hasNoRequiredTasks);
    insertParams.put(DBConstants.USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE, hasNoRequiredDefeatTypeJobs);

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }
  
  /* used for quest defeat type jobs*/
  public static boolean insertCompletedDefeatTypeJobIdForUserQuest(int userId, int dtjId, int questId) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID, dtjId);

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /* used for quest tasks*/
  public static boolean insertCompletedTaskIdForUserQuest(int userId, int taskId, int questId) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__USER_ID, userId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__QUEST_ID, questId);
    insertParams.put(DBConstants.USER_QUESTS_COMPLETED_TASKS__COMPLETED_TASK_ID, taskId);

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_USER_QUESTS_COMPLETED_TASKS, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  /*
   * returns the id of the userstruct, -1 if none
   */
  public static int insertUserStruct(int userId, int structId, CoordinatePair coordinates, Timestamp timeOfPurchase) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    insertParams.put(DBConstants.USER_STRUCTS__STRUCT_ID, structId);
    insertParams.put(DBConstants.USER_STRUCTS__X_COORD, coordinates.getX());
    insertParams.put(DBConstants.USER_STRUCTS__Y_COORD, coordinates.getY());
    insertParams.put(DBConstants.USER_STRUCTS__PURCHASE_TIME, timeOfPurchase);

    int userStructId = DBConnection.insertIntoTableBasicReturnId(DBConstants.TABLE_USER_STRUCTS, insertParams);
    return userStructId;
  }

  public static boolean insertIAPHistoryElem(JSONObject appleReceipt, int diamondChange, User user, double cashCost) {
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

      if (appleReceipt.getString(IAPValues.APP_ITEM_ID) != null) {
        insertParams.put(DBConstants.IAP_HISTORY__APP_ITEM_ID, appleReceipt.getString(IAPValues.APP_ITEM_ID));
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return false;
    }
    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_IAP_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }
    return false;
  }

  public static boolean insertMarketplaceItem(int posterId, MarketplacePostType postType, 
      int postedEquipId, int diamondCost, int coinCost) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    insertParams.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    insertParams.put(DBConstants.MARKETPLACE__POST_TYPE, postType.getNumber());
    insertParams.put(DBConstants.MARKETPLACE__POSTED_EQUIP_ID, postedEquipId);

    if (diamondCost > 0){
      insertParams.put(DBConstants.MARKETPLACE__DIAMOND_COST, diamondCost);
    }
    if (coinCost > 0) {
      insertParams.put(DBConstants.MARKETPLACE__COIN_COST, coinCost);
    }

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_MARKETPLACE, insertParams);
    if (numInserted == 1) {
      return true;
    }

    return false;
  }


  public static boolean insertMarketplaceItemIntoHistory(MarketplacePost mp,
      int buyerId) {
    Map <String, Object> insertParams = new HashMap<String, Object>();

    MarketplacePostType postType = mp.getPostType();

    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__MARKETPLACE_ID, mp.getId());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID, mp.getPosterId());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__BUYER_ID, buyerId);
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POST_TYPE, postType.getNumber());
    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_POST, mp.getTimeOfPost());

    insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_ID, mp.getPostedEquipId());
    if (mp.getDiamondCost() > 0){
      insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__DIAMOND_COST, mp.getDiamondCost());
    }
    if (mp.getCoinCost() > 0) {
      insertParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__COIN_COST, mp.getCoinCost());
    }

    int numInserted = DBConnection.insertIntoTableBasic(DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY, insertParams);
    if (numInserted == 1) {
      return true;
    }

    return false;
  }
}
