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
import com.lvl6.utils.DBConnection;

public class UserRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER;
  
  public static boolean createUser() {
    log.info("creating user");
    //TODO: impl
    //when you select your class/name on the last page of the tutorial, THEN i add you to the db
    //then i add the udid, class, name, etc.
    return false;
  }

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
    return convertRSToUsers(DBConnection.selectRowsOr(paramsToVals, TABLE_NAME));
  }

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static User getUserByUDID(String UDID) {
    log.info("retrieving user with udid " + UDID);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__UDID, UDID);
    return convertRSToUser(DBConnection.selectRowsOr(paramsToVals, "users"));
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
        System.out.println("problem with database call.");
        e.printStackTrace();
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
        System.out.println("problem with database call.");
        e.printStackTrace();
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
    User user = new User(userId, name, level, type, attack, defense, stamina, energy, health, skillPoints, 
        healthMax, energyMax, staminaMax, diamonds, coins, wood, vaultBalance, experience, tasksCompleted, 
        battlesWon, battlesLost, hourlyCoins, armyCode, numReferrals, udid, userLocation);
    return user;
  }
}
