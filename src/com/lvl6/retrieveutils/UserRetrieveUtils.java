package com.lvl6.retrieveutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.User;
import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;
import com.lvl6.utils.DBConnection;

public class UserRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = "users";
  
  public static void createUser() {
    log.info("creating user");
    //TODO: impl
    //when you select your class/name on the last page of the tutorial, THEN i add you to the db
    //then i add the udid, class, name, etc.
  }

  public static User getUserById(int userId) {
    log.info("retrieving user with userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("id", userId);
    return convertRSToUser(DBConnection.selectRowById(userId, TABLE_NAME));
  }
  
  public static List<User> getUsersByIds(List<Integer> ids) {
    log.info("retrieving users with userIds " + ids);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    for (Integer i : ids) {
      paramsToVals.put("id", i);
    }
    return convertRSToUsers(DBConnection.selectRowsOr(paramsToVals, TABLE_NAME));
  }

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static User getUserByUDID(String UDID) {
    log.info("retrieving user with udid " + UDID);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("udid", UDID);
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
    int userId = rs.getInt(1);
    String name = rs.getString(2);
    int level = rs.getInt(3);
    UserType type = UserType.valueOf(rs.getInt(4));
    int attack = rs.getInt(5);
    int defense = rs.getInt(6);
    int stamina = rs.getInt(7);
    int energy = rs.getInt(8);
    int health = rs.getInt(9);
    int skillPoints = rs.getInt(10);
    int healthMax = rs.getInt(11);
    int energyMax = rs.getInt(12);
    int staminaMax = rs.getInt(13);
    int diamonds = rs.getInt(14);
    int coins = rs.getInt(15);
    int vaultBalance = rs.getInt(16);
    int experience = rs.getInt(17);
    int tasksCompleted = rs.getInt(18);
    int battlesWon = rs.getInt(19);
    int battlesLost = rs.getInt(20);
    int hourlyCoins = rs.getInt(21);
    String armyCode = rs.getString(22);
    int armySize = rs.getInt(23);
    String udid = rs.getString(24);
    User user = new User(userId, name, level, type, attack, defense, stamina, energy, health, skillPoints, 
        healthMax, energyMax, staminaMax, diamonds, coins, vaultBalance, experience, tasksCompleted, 
        battlesWon, battlesLost, hourlyCoins, armyCode, armySize, udid);
    return user;
  }
}
