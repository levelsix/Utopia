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

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.StringUtils;

public class UserStructRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_STRUCTS;

  public static List<UserStruct> getUserStructsForUser(int userId) {
    log.info("retrieving user structs for userId " + userId);
    return convertRSToUserStructs(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }

  public static Map<Integer, List<UserStruct>> getStructIdsToUserStructsForUser(int userId) {
    log.info("retrieving user structs for userId " + userId);
    return convertRSToStructIdsToUserStructs(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }

  public static UserStruct getSpecificUserStruct(int userStructId) {
    log.info("retrieving user structs for user struct id " + userStructId);
    return convertRSSingleToUserStructs(DBConnection.selectRowsById(userStructId, TABLE_NAME));
  }

  public static List<UserStruct> getUserStructs(List<Integer> userStructIds) {
    if (userStructIds == null || userStructIds.size() <= 0 ) {
      return new ArrayList<UserStruct>();
    }
    
    String query = "select * from " + TABLE_NAME + " where (";
    List<String> condClauses = new ArrayList<String>();
    List <Object> values = new ArrayList<Object>();
    for (Integer userStructId : userStructIds) {
      condClauses.add(DBConstants.USER_STRUCTS__ID + "=?");
      values.add(userStructId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";
    return convertRSToUserStructs(DBConnection.selectDirectQueryNaive(query, values));
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


  private static Map<Integer, List<UserStruct>> convertRSToStructIdsToUserStructs(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<UserStruct>> structIdsToUserStructs = new HashMap<Integer, List<UserStruct>>();
        while(rs.next()) {
          UserStruct userStruct = convertRSRowToUserStruct(rs);
          List<UserStruct> userStructsForStructId = structIdsToUserStructs.get(userStruct.getStructId());
          if (userStructsForStructId != null) {
            userStructsForStructId.add(userStruct);
          } else {
            List<UserStruct> userStructs = new ArrayList<UserStruct>();
            userStructs.add(userStruct);
            structIdsToUserStructs.put(userStruct.getStructId(), userStructs);
          }
        }
        return structIdsToUserStructs;
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
    
    Date lastUpgradeTime = null;
    Timestamp ts2 = rs.getTimestamp(i++);
    if (ts2 != null) {
      lastUpgradeTime = new Date(ts2.getTime());
    }
    
    boolean isComplete = rs.getBoolean(i++);
    StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));

    return new UserStruct(id, userId, structId, lastRetrieved, coordinates, level, purchaseTime, lastUpgradeTime, isComplete, orientation);
  }

}
