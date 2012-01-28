package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class UserStructRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_STRUCTS;
  
  public static List<UserStruct> getUserStructsForUser(int userId) {
    log.info("retrieving user structs for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_STRUCTS__USER_ID, userId);
    return convertRSToUserStructs(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }
  
  public static UserStruct getSpecificUserStruct(int userStructId) {
    log.info("retrieving user structs for user struct id " + userStructId);
    return convertRSSingleToUserStructs(DBConnection.selectRowsById(userStructId, TABLE_NAME));
  }
  
  private static List<UserStruct> convertRSToUserStructs(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserStruct> userStructs = new ArrayList<UserStruct>();
        while(rs.next()) {
          userStructs.add(convertRSRowToUserStruct(rs));
        }
        return userStructs;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
  private static UserStruct convertRSSingleToUserStructs(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          return convertRSRowToUserStruct(rs);
        }
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
  private static UserStruct convertRSRowToUserStruct(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int structId = rs.getInt(i++);
    Date lastRetrieved = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (ts != null) {
      lastRetrieved = new Date(ts.getTime());
    }
    CoordinatePair coordinates = new CoordinatePair(rs.getInt(i++), rs.getInt(i++));
    int level = rs.getInt(i++);
    Date purchaseTime = new Date(rs.getTimestamp(i++).getTime());
    boolean isComplete = rs.getBoolean(i++);
    
    return new UserStruct(id, userId, structId, lastRetrieved, coordinates, level, purchaseTime, isComplete);
  }
  
}
