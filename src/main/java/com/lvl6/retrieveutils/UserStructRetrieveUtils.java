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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.StringUtils;

@Component @DependsOn("gameServer") public class UserStructRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_STRUCTS;

  public List<UserStruct> getUserStructsForUser(int userId) {
    log.debug("retrieving user structs for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<UserStruct> userStructs = convertRSToUserStructs(rs);
    DBConnection.get().close(rs, null, conn);
    return userStructs;
  }

  
  ////@Cacheable(value="structIdsToUserStructsForUser", key="#userId")
  public Map<Integer, List<UserStruct>> getStructIdsToUserStructsForUser(int userId) {
    log.debug("retrieving map of struct id to userstructs for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, List<UserStruct>> structIdToUserStructs = convertRSToStructIdsToUserStructs(rs);
    DBConnection.get().close(rs, null, conn);
    return structIdToUserStructs;
  }

  ////@Cacheable(value="specificUserStruct", key="#userStructId")
  public UserStruct getSpecificUserStruct(int userStructId) {
    log.debug("retrieving user struct with id " + userStructId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, userStructId, TABLE_NAME);
    UserStruct userStruct = convertRSSingleToUserStructs(rs);
    DBConnection.get().close(rs, null, conn);
    return userStruct;
  }

  
  public List<UserStruct> getUserStructs(List<Integer> userStructIds) {
    log.debug("retrieving userStructs with ids " + userStructIds);
    
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
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    List<UserStruct> userStructs = convertRSToUserStructs(rs);
    DBConnection.get().close(rs, null, conn);
    return userStructs;
  }

  private List<UserStruct> convertRSToUserStructs(ResultSet rs) {
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
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }


  private Map<Integer, List<UserStruct>> convertRSToStructIdsToUserStructs(
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
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private UserStruct convertRSSingleToUserStructs(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          return convertRSRowToUserStruct(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private UserStruct convertRSRowToUserStruct(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int structId = rs.getInt(i++);
    
    Date lastRetrieved = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastRetrieved = new Date(ts.getTime());
    }
    
    CoordinatePair coordinates = new CoordinatePair(rs.getInt(i++), rs.getInt(i++));
    int level = rs.getInt(i++);
    Date purchaseTime = new Date(rs.getTimestamp(i++).getTime());
    
    Date lastUpgradeTime = null;
    ts= rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastUpgradeTime = new Date(ts.getTime());
    }
    
    boolean isComplete = rs.getBoolean(i++);
    StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));

    return new UserStruct(id, userId, structId, lastRetrieved, coordinates, level, purchaseTime, lastUpgradeTime, isComplete, orientation);
  }

}
