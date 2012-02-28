package com.lvl6.retrieveutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.StringUtils;

public class UserRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER;

  private static final int BATTLE_INITIAL_LEVEL_RANGE = 10;    //even number makes it more consistent. ie 6 would be +/- 3 levels from user level
  private static final int BATTLE_INITIAL_RANGE_INCREASE = 4;    //even number better again
  private static final int BATTLE_RANGE_INCREASE_MULTIPLE = 3;
  private static final int MAX_BATTLE_DB_HITS = 5;

  public static User getUserById(int userId) {
    log.info("retrieving user with userId " + userId);
    return convertRSToUser(DBConnection.selectRowsById(userId, TABLE_NAME));
  }

  public static Map<Integer, User> getUsersByIds(List<Integer> userIds) {
    if (userIds == null || userIds.size() <= 0 ) {
      return new HashMap<Integer, User>();
    }

    String query = "select * from " + TABLE_NAME + " where (";
    List<String> condClauses = new ArrayList<String>();
    List <Object> values = new ArrayList<Object>();
    for (Integer userId : userIds) {
      condClauses.add(DBConstants.USER__ID + "=?");
      values.add(userId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";
    return convertRSToUserIdToUsersMap(DBConnection.selectDirectQueryNaive(query, values));
  }

  public static List<User> getUsers(List<UserType> requestedTypes, int numUsers, int playerLevel, int userId, boolean guaranteeNum, 
      Integer latLowerBound, Integer latUpperBound, Integer longLowerBound, Integer longUpperBound, boolean forBattle) {
    log.info("retrieving list of users for user " + userId);

    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2, ControllerConstants.BATTLE__MIN_BATTLE_LEVEL);
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2;

    List <Object> values = new ArrayList<Object>();

    String query = "select * from " + TABLE_NAME + " where ";
    
    if (requestedTypes != null && requestedTypes.size() > 0) {
      query += "(";
      for (int i = 0; i < requestedTypes.size(); i++) {
        values.add(requestedTypes.get(i).getNumber());
        if (i == requestedTypes.size() - 1) {
          query += DBConstants.USER__TYPE + "=?";
        } else {
          query += DBConstants.USER__TYPE + "=?  or ";
        }
      }
      query += ") and ";
    }

    if (latLowerBound != null) {
      query += DBConstants.USER__LATITUDE + ">=? and ";
      values.add(latLowerBound);
    }
    if (latUpperBound != null) {
      query += DBConstants.USER__LATITUDE + "<=? and ";
      values.add(latUpperBound);
    }
    if (longLowerBound != null) {
      query += DBConstants.USER__LONGITUDE + ">=? and ";
      values.add(latLowerBound);
    }
    if (longUpperBound != null) {
      query += DBConstants.USER__LONGITUDE + "<=? and ";
      values.add(latLowerBound);
    }
    
    if (forBattle) {
      query += DBConstants.USER__LAST_BATTLE_NOTIFICATION_TIME + "<=? and ";
      values.add(new Timestamp(ControllerConstants.NUM_MINUTES_SINCE_LAST_BATTLE_BEFORE_APPEARANCE_IN_ATTACK_LISTS*60000));
    }
    
    query += DBConstants.USER__LEVEL + ">=? and " + DBConstants.USER__LEVEL + "<=? order by rand()";

    values.add(levelMin);
    values.add(levelMax);

    int rangeIncrease = BATTLE_INITIAL_RANGE_INCREASE;
    int numDBHits = 1;
    ResultSet rs = DBConnection.selectDirectQueryNaive(query, values);
    while (rs != null && MiscMethods.getRowCount(rs) < numUsers) {
      values.remove(values.size()-1);
      values.remove(values.size()-1);
      values.add(Math.max(ControllerConstants.BATTLE__MIN_BATTLE_LEVEL, levelMin - rangeIncrease/2));
      values.add(levelMax + rangeIncrease/2);
      rs = DBConnection.selectDirectQueryNaive(query, values);
      numDBHits++;
      if (!guaranteeNum) {
        if (numDBHits == MAX_BATTLE_DB_HITS) break;
      }
      rangeIncrease *= BATTLE_RANGE_INCREASE_MULTIPLE;
    }
    return convertRSToUsers(rs);
  }

  /*
  public static List<User> getUsersForSide(boolean generateListOfGoodSide, int numUsers, int playerLevel, int userId, boolean guaranteeNum) {
    log.info("retrieving list of users for user " + userId);

    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2, ControllerConstants.BATTLE__MIN_BATTLE_LEVEL);
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2;

    String query = "select * from " + TABLE_NAME + " where (" + DBConstants.USER__TYPE + 
        "=? or " + DBConstants.USER__TYPE + "=? or " + DBConstants.USER__TYPE + "=?) and " +
        DBConstants.USER__LEVEL + ">=? and " + DBConstants.USER__LEVEL + "<=? order by rand()";

    List <Object> values = new ArrayList<Object>();
    if (generateListOfGoodSide) {
      values.add(UserType.GOOD_ARCHER_VALUE);
      values.add(UserType.GOOD_MAGE_VALUE);
      values.add(UserType.GOOD_WARRIOR_VALUE);
    } else {
      values.add(UserType.BAD_ARCHER_VALUE);
      values.add(UserType.BAD_MAGE_VALUE);
      values.add(UserType.BAD_WARRIOR_VALUE);      
    }
    values.add(levelMin);
    values.add(levelMax);

    int rangeIncrease = BATTLE_INITIAL_RANGE_INCREASE;
    int numDBHits = 1;
    ResultSet rs = DBConnection.selectDirectQueryNaive(query, values);
    while (rs != null && MiscMethods.getRowCount(rs) < numUsers) {
      values.remove(values.size()-1);
      values.remove(values.size()-1);
      values.add(Math.max(ControllerConstants.BATTLE__MIN_BATTLE_LEVEL, levelMin - rangeIncrease/2));
      values.add(levelMax + rangeIncrease/2);
      rs = DBConnection.selectDirectQueryNaive(query, values);
      numDBHits++;
      if (!guaranteeNum) {
        if (numDBHits == MAX_BATTLE_DB_HITS) break;
      }
      rangeIncrease *= BATTLE_RANGE_INCREASE_MULTIPLE;
    }
    return convertRSToUsers(rs);
  }*/

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static User getUserByUDID(String UDID) {
    log.info("retrieving user with udid " + UDID);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__UDID, UDID);
    return convertRSToUser(DBConnection.selectRowsAbsoluteOr(paramsToVals, "users"));
  }

  private static User convertRSToUser(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          return convertRSRowToUser(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }


  private static Map<Integer, User> convertRSToUserIdToUsersMap(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, User> userIdsToUsers = new HashMap<Integer, User>();
        while(rs.next()) {
          User user = convertRSRowToUser(rs);
          userIdsToUsers.put(user.getId(), user);
        }
        return userIdsToUsers;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static List<User> convertRSToUsers(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<User> users = new ArrayList<User>();
        while(rs.next()) {  //should only be one
          users.add(convertRSRowToUser(rs));
        }
        return users;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static User convertRSRowToUser(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    String name = rs.getString(i++);
    int level = rs.getInt(i++);
    UserType type = UserType.valueOf(rs.getInt(i++));
    int attack = rs.getInt(i++);
    int defense = rs.getInt(i++);
    int stamina = rs.getInt(i++);

    Date lastStaminaRefillTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastStaminaRefillTime = new Date(ts.getTime());
    }

    boolean isLastStaminaStateFull = rs.getBoolean(i++);
    int energy = rs.getInt(i++);

    Date lastEnergyRefillTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastEnergyRefillTime = new Date(ts.getTime());
    }

    boolean isLastEnergyStateFull = rs.getBoolean(i++);
    int skillPoints = rs.getInt(i++);
    int healthMax = rs.getInt(i++);
    int energyMax = rs.getInt(i++);
    int staminaMax = rs.getInt(i++);
    int diamonds = rs.getInt(i++);
    int coins = rs.getInt(i++);
    int marketplaceDiamondsEarnings = rs.getInt(i++);
    int marketplaceCoinsEarnings = rs.getInt(i++);
    int vaultBalance = rs.getInt(i++);
    int experience = rs.getInt(i++);
    int tasksCompleted = rs.getInt(i++);
    int battlesWon = rs.getInt(i++);
    int battlesLost = rs.getInt(i++);
    int flees = rs.getInt(i++);
    int hourlyCoins = rs.getInt(i++);
    String armyCode = rs.getString(i++);
    int numReferrals = rs.getInt(i++);
    String udid = rs.getString(i++);
    Location userLocation = new Location(rs.getDouble(i++), rs.getDouble(i++));
    int numPostsInMarketplace = rs.getInt(i++);
    int numMarketplaceSalesUnredeemed = rs.getInt(i++);

    int weaponEquipped = rs.getInt(i++);
    if (rs.wasNull()) {
      weaponEquipped = ControllerConstants.NOT_SET;
    }
    int armorEquipped = rs.getInt(i++);
    if (rs.wasNull()) {
      armorEquipped = ControllerConstants.NOT_SET;
    }
    int amuletEquipped = rs.getInt(i++);
    if (rs.wasNull()) {
      amuletEquipped = ControllerConstants.NOT_SET;
    }

    Date lastLoginTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLoginTime = new Date(ts.getTime());
    }

    Date lastLogoutTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLogoutTime = new Date(ts.getTime());
    }

    String deviceToken = rs.getString(i++);

    Date lastBattleNotificationTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastBattleNotificationTime = new Date(ts.getTime());
    }

    Date lastTimeAttacked = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastTimeAttacked = new Date(ts.getTime());
    }

    String macAddress = rs.getString(i++);
    int numBadges = rs.getInt(i++);

    Date lastShortLicensePurchaseTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastShortLicensePurchaseTime = new Date(ts.getTime());
    }

    Date lastLongLicensePurchaseTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLongLicensePurchaseTime = new Date(ts.getTime());
    }

    User user = new User(userId, name, level, type, attack, defense, stamina, lastStaminaRefillTime, isLastStaminaStateFull, energy, lastEnergyRefillTime, 
        isLastEnergyStateFull, skillPoints, healthMax, energyMax, staminaMax, diamonds, coins, marketplaceDiamondsEarnings, marketplaceCoinsEarnings, 
        vaultBalance, experience, tasksCompleted, battlesWon, battlesLost, flees,
        hourlyCoins, armyCode, numReferrals, udid, userLocation, numPostsInMarketplace, numMarketplaceSalesUnredeemed, 
        weaponEquipped, armorEquipped, amuletEquipped, lastLoginTime, lastLogoutTime, deviceToken, 
        lastBattleNotificationTime, lastTimeAttacked, macAddress, numBadges, lastShortLicensePurchaseTime, lastLongLicensePurchaseTime);
    return user;
  }
}
