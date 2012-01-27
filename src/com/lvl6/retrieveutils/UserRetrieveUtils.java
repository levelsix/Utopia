package com.lvl6.retrieveutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.server.controller.BattleController;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.MiscMethods;

public class UserRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER;
  
  private static final int BATTLE_INITIAL_LEVEL_RANGE = 10;    //even number makes it more consistent. ie 6 would be +/- 3 levels from user level
  private static final int BATTLE_INITIAL_RANGE_INCREASE = 4;    //even number better again
  private static final int BATTLE_RANGE_INCREASE_MULTIPLE = 3;
  private static final int MIN_BATTLE_HEALTH_REQUIREMENT = BattleController.MIN_BATTLE_HEALTH_REQUIREMENT;
  private static final int MAX_BATTLE_DB_HITS = 5;

  public static User getUserById(int userId) {
    log.info("retrieving user with userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER__ID, userId);
    return convertRSToUser(DBConnection.selectRowsById(userId, TABLE_NAME));
  }
  
  public static List<User> getUsersByIds(List<Integer> ids) {
    log.info("retrieving users with userIds " + ids);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    for (Integer i : ids) {
      paramsToVals.put(DBConstants.USER__ID, i);
    }
    return convertRSToUsers(DBConnection.selectRowsAbsoluteOr(paramsToVals, TABLE_NAME));
  }
  
  public static List<User> getUsersForSide(boolean generateListOfGoodSide, int numUsers, int playerLevel, int userId) {
    log.info("retrieving list of enemies for user " + userId);
    
    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2, BattleController.MIN_BATTLE_LEVEL);
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2;
    
    String query = "select * from " + TABLE_NAME + " where "+ DBConstants.USER__HEALTH + ">= "+ 
        MIN_BATTLE_HEALTH_REQUIREMENT +" and (" + DBConstants.USER__TYPE + 
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
      values.add(Math.max(BattleController.MIN_BATTLE_LEVEL, levelMin - rangeIncrease/2));
      values.add(levelMax + rangeIncrease/2);
      rs = DBConnection.selectDirectQueryNaive(query, values);
      numDBHits++;
      if (numDBHits == MAX_BATTLE_DB_HITS) break;
      rangeIncrease *= BATTLE_RANGE_INCREASE_MULTIPLE;
    }
    return convertRSToUsers(rs);
  }

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
    int energy = rs.getInt(i++);
    int health = rs.getInt(i++);
    int skillPoints = rs.getInt(i++);
    int healthMax = rs.getInt(i++);
    int energyMax = rs.getInt(i++);
    int staminaMax = rs.getInt(i++);
    int diamonds = rs.getInt(i++);
    int coins = rs.getInt(i++);
    int wood = rs.getInt(i++);
    int vaultBalance = rs.getInt(i++);
    int experience = rs.getInt(i++);
    int tasksCompleted = rs.getInt(i++);
    int battlesWon = rs.getInt(i++);
    int battlesLost = rs.getInt(i++);
    int hourlyCoins = rs.getInt(i++);
    String armyCode = rs.getString(i++);
    int numReferrals = rs.getInt(i++);
    String udid = rs.getString(i++);
    Location userLocation = new Location(rs.getFloat(i++), rs.getFloat(i++));
    int numPostsInMarketplace = rs.getInt(i++);
    User user = new User(userId, name, level, type, attack, defense, stamina, energy, health, skillPoints, 
        healthMax, energyMax, staminaMax, diamonds, coins, wood, vaultBalance, experience, tasksCompleted, 
        battlesWon, battlesLost, hourlyCoins, armyCode, numReferrals, udid, userLocation, numPostsInMarketplace);
    return user;
  }
}
