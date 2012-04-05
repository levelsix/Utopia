package com.lvl6.retrieveutils;
import java.sql.Connection;
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

  private static final int BATTLE_INITIAL_LEVEL_RANGE = 6;    //even number makes it more consistent. ie 6 would be +/- 3 levels from user level
  private static final int BATTLE_INITIAL_RANGE_INCREASE = 4;    //even number better again
  private static final int BATTLE_RANGE_INCREASE_MULTIPLE = 3;
  private static final int MAX_BATTLE_DB_HITS = 5;

  public static User getUserById(int userId) {
    log.info("retrieving user with userId " + userId);

    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsById(conn, userId, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.close(rs, null, conn);
    return user;
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

    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectDirectQueryNaive(conn, query, values);
    Map<Integer, User> userIdToUserMap = convertRSToUserIdToUsersMap(rs);
    DBConnection.close(rs, null, conn);
    return userIdToUserMap;
  }

  public static List<User> getUsers(List<UserType> requestedTypes, int numUsers, int playerLevel, int userId, boolean guaranteeNum, 
      Integer latLowerBound, Integer latUpperBound, Integer longLowerBound, Integer longUpperBound, boolean forBattle) {
    log.info("retrieving list of users for user " + userId);

    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2, 2) + 1;
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2 + 1;

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
      values.add(longLowerBound);
    }
    if (longUpperBound != null) {
      query += DBConstants.USER__LONGITUDE + "<=? and ";
      values.add(longUpperBound);
    }

    if (forBattle) {
      query += "(" + DBConstants.USER__LAST_TIME_ATTACKED + "<=? or " +  DBConstants.USER__LAST_TIME_ATTACKED + " is ?) and ";
      values.add(new Timestamp(new Date().getTime() - ControllerConstants.NUM_MINUTES_SINCE_LAST_BATTLE_BEFORE_APPEARANCE_IN_ATTACK_LISTS*60000));
      values.add(null);
    }

    query += DBConstants.USER__LEVEL + ">=? and " + DBConstants.USER__LEVEL + "<=? ";
    values.add(levelMin);
    values.add(levelMax);

    query += "order by " + DBConstants.USER__IS_FAKE + ", rand() limit ?";
    values.add(numUsers);

    int rangeIncrease = BATTLE_INITIAL_RANGE_INCREASE;
    int numDBHits = 1;

    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectDirectQueryNaive(conn, query, values);
      while (rs != null && MiscMethods.getRowCount(rs) < numUsers) {
        values.remove(values.size()-1);
        values.remove(values.size()-1);
        values.remove(values.size()-1);
        values.add(Math.max(2, levelMin - rangeIncrease/2));
        values.add(levelMax + rangeIncrease/2);
        values.add(numUsers);
        rs = DBConnection.selectDirectQueryNaive(conn, query, values);
        numDBHits++;
        if (!guaranteeNum) {
          if (numDBHits == MAX_BATTLE_DB_HITS) break;
        }
        rangeIncrease *= BATTLE_RANGE_INCREASE_MULTIPLE;
      }
    }
    
    List<User> users = convertRSToUsers(rs);
    if (users == null) users = new ArrayList<User>();
    DBConnection.close(rs, null, conn);
    return users;
  }

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static User getUserByUDID(String UDID) {
    log.info("retrieving user with udid " + UDID);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__UDID, UDID);

    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.close(rs, null, conn);
    return user;
  }

  public static User getUserByReferralCode(String referralCode) {
    log.info("retrieving user with referral code " + referralCode);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__REFERRAL_CODE, referralCode);

    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.close(rs, null, conn);
    return user;
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
          if (user != null) {
            userIdsToUsers.put(user.getId(), user);
          }
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

    int energy = rs.getInt(i++);

    Date lastEnergyRefillTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastEnergyRefillTime = new Date(ts.getTime());
    }

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
    String referralCode = rs.getString(i++);
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

    boolean isFake = rs.getBoolean(i++);

    User user = new User(userId, name, level, type, attack, defense, stamina, lastStaminaRefillTime, energy, lastEnergyRefillTime, 
        skillPoints, healthMax, energyMax, staminaMax, diamonds, coins, marketplaceDiamondsEarnings, marketplaceCoinsEarnings, 
        vaultBalance, experience, tasksCompleted, battlesWon, battlesLost, flees,
        referralCode, numReferrals, udid, userLocation, numPostsInMarketplace, numMarketplaceSalesUnredeemed, 
        weaponEquipped, armorEquipped, amuletEquipped, lastLoginTime, lastLogoutTime, deviceToken, 
        lastBattleNotificationTime, lastTimeAttacked, numBadges, lastShortLicensePurchaseTime, lastLongLicensePurchaseTime, isFake);
    return user;
  }
}
