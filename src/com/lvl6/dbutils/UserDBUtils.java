package com.lvl6.dbutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.User;
import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;
import com.lvl6.utils.DBConnection;

public class UserDBUtils {

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
    UserType type = UserType.valueOf(rs.getInt(3));
    String udid = rs.getString(4);
    User user = new User(userId, name, type, udid);
    return user;
  }
}
