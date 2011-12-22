package com.lvl6.info;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import com.lvl6.utils.DBConnection;

public class UserInfo{

  private int userId;
  private String udid;

  public static void createUser() {
    //when you select your class/name on the last page of the tutorial, THEN i add you to the db
    //then i add the udid, class, name, etc.
  }

  public static UserInfo getUserById(int userId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("id", userId);
    return convertRSToUser(DBConnection.selectRowById(userId, "users"));
  }

  //when you first log in, call this
  //if this returns null, tell user it's the player's first time/launch tutorial
  public static UserInfo getUserByUDID(String UDID) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("udid", UDID);
    return convertRSToUser(DBConnection.selectRows(paramsToVals, "users"));
  }

  public static UserInfo convertRSToUser(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        UserInfo userInfo = new UserInfo();
        while(rs.next()) {  //should only be one
          int userId = rs.getInt(1);
          String udid = rs.getString(2);
          userInfo.setUserId(userId);
          userInfo.setUdid(udid);
        }
        return userInfo;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }
    return null;
  }
  
  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUdid() {
    return udid;
  }

  public void setUdid(String udid) {
    this.udid = udid;
  }


}
