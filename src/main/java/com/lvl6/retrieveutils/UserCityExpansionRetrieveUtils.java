package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserCityExpansionData;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserCityExpansionRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_EXPANSIONS;

  public static UserCityExpansionData getUserCityExpansionDataForUser(int userId) {
    log.debug("retrieving user city expansion data for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.GENERIC__USER_ID, userId);
    
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
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static UserCityExpansionData getUserCityExpansionDataFromRSRow(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int farLeftExpansions = rs.getInt(i++);
    int farRightExpansions = rs.getInt(i++);
    int nearLeftExpansions = rs.getInt(i++);
    int nearRightExpansions = rs.getInt(i++);
    boolean isExpanding = rs.getBoolean(i++);

    Date lastExpandTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastExpandTime = new Date(ts.getTime());
    }
    
    int lastExpandDirectionInt = rs.getInt(i++);
    ExpansionDirection lastExpandDirection = (rs.wasNull()) ? null : ExpansionDirection.valueOf(lastExpandDirectionInt);

    return new UserCityExpansionData(userId, farLeftExpansions, farRightExpansions, nearLeftExpansions, nearRightExpansions, isExpanding, lastExpandTime, lastExpandDirection);
  }
}
