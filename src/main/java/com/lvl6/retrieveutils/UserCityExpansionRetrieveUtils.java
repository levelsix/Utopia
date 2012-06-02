package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.UserCityExpansionData;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.utils.DBConnection;

public class UserCityExpansionRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_CITY_ELEMS;

  public static UserCityExpansionData getUserCityExpansionDataForUser(int userId) {
    log.debug("retrieving user city expansion data for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);
    
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    UserCityExpansionData userCityExpansionData = convertRSToUserCritstructs(rs);
    DBConnection.get().close(rs, null, conn);
    return userCityExpansionData;
  }

  private static UserCityExpansionData convertRSToUserCritstructs(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          return getUserCityExpansionDataFromRSRow(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static UserCityExpansionData getUserCityExpansionDataFromRSRow(ResultSet rs) throws SQLException {
    int userId = rs.getInt(DBConstants.USER_CITY_ELEMS__USER_ID);
    int farLeftExpansions = rs.getInt(DBConstants.USER_CITY_ELEMS__FAR_LEFT_EXPANSIONS);
    int farRightExpansions = rs.getInt(DBConstants.USER_CITY_ELEMS__FAR_RIGHT_EXPANSIONS);
    boolean isExpanding = rs.getBoolean(DBConstants.USER_CITY_ELEMS__IS_EXPANDING);
    
    long lastExpandTimeLong = rs.getLong(DBConstants.USER_CITY_ELEMS__LAST_EXPAND_TIME);
    Timestamp lastExpandTime = (rs.wasNull()) ? null : new Timestamp(lastExpandTimeLong);
    
    int lastExpandDirectionInt = rs.getInt(DBConstants.USER_CITY_ELEMS__LAST_EXPAND_TIME);
    ExpansionDirection lastExpandDirection = (rs.wasNull()) ? null : ExpansionDirection.valueOf(lastExpandDirectionInt);

    return new UserCityExpansionData(userId, farLeftExpansions, farRightExpansions, isExpanding, lastExpandTime, lastExpandDirection);
  }
}
