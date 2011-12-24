package com.lvl6.dbutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import com.lvl6.info.User;
import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;
import com.lvl6.utils.DBConnection;

public class UserDBUtils {

  public static void createUser() {
    //when you select your class/name on the last page of the tutorial, THEN i add you to the db
    //then i add the udid, class, name, etc.
  }

  public static User getUserById(int userId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("id", userId);
    return convertRSToUserBuilder(DBConnection.selectRowById(userId, "users"));
  }

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static User getUserByUDID(String UDID) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("udid", UDID);
    return convertRSToUserBuilder(DBConnection.selectRows(paramsToVals, "users"));
  }

  public static User convertRSToUserBuilder(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          int userId = rs.getInt(1);
          String name = rs.getString(2);
          UserType type = UserType.valueOf(rs.getInt(3));
          String udid = rs.getString(4);
          User user = new User(userId, name, type, udid);
          return user;
        }
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }
    return null;
  }
}
