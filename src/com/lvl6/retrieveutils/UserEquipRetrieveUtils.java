package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.UserEquip;
import com.lvl6.utils.DBConnection;

public class UserEquipRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = "user_equip";
  
  public static List<UserEquip> getUserEquipsForUser(int userId) {
    log.info("retrieving user equips for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put("user_id", userId);
    return convertRSToUserEquips(DBConnection.selectRowByUserId(userId, TABLE_NAME));
  }
  
  //returns map from userId to his equipments
  public static Map<Integer, List<UserEquip>> getUserEquipsForUserIds(List<Integer> userIds) {
    log.info("retrieving user equips for userIds " + userIds);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    for (Integer i : userIds) {
      paramsToVals.put("user_id", i);
    }
    return convertRSToUserToUserEquips(DBConnection.selectRowsOr(paramsToVals, TABLE_NAME));
  }
  
  private static Map<Integer, List<UserEquip>> convertRSToUserToUserEquips(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<UserEquip>> userToUserEquips = new HashMap<Integer, List<UserEquip>>();
        while(rs.next()) {  //should only be one
          UserEquip ue = convertRSRowToUserEquip(rs);
          List<UserEquip> userUEs = userToUserEquips.get(ue.getUserId());
          if (userUEs != null) {
            userUEs.add(ue);
          } else {
            List<UserEquip> ues = new ArrayList<UserEquip>();
            ues.add(ue);
            userToUserEquips.put(ue.getUserId(), ues);
          }
        }
        return userToUserEquips;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }
    return null;
  }
  
  private static List<UserEquip> convertRSToUserEquips(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserEquip> userEquips = new ArrayList<UserEquip>();
        while(rs.next()) {  //should only be one
          userEquips.add(convertRSRowToUserEquip(rs));
        }
        return userEquips;
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
  private static UserEquip convertRSRowToUserEquip(ResultSet rs) throws SQLException {
    int userId = rs.getInt(1);
    int equipId = rs.getInt(2);
    int quantity = rs.getInt(3);
    boolean isStolen = rs.getBoolean(4);
    UserEquip userEquip = new UserEquip(userId, equipId, quantity, isStolen);
    return userEquip;
  }
  
}
