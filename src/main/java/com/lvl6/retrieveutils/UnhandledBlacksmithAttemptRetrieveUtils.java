package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UnhandledBlacksmithAttemptRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_BLACKSMITH;

  public static List<BlacksmithAttempt> getUnhandledBlacksmithAttemptsForUser(int userId) {
    log.debug("retrieving unhandled blacksmith attempts for user " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<BlacksmithAttempt> blacksmithAttempts = convertRSToBlacksmithAttempts(rs);
    DBConnection.get().close(rs, null, conn);
    return blacksmithAttempts;
  }
  
  private static List<BlacksmithAttempt> convertRSToBlacksmithAttempts(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<BlacksmithAttempt> blacksmithAttemptList = new ArrayList<BlacksmithAttempt>();
        while(rs.next()) {
          BlacksmithAttempt blacksmithAttempt = convertRSRowToBlacksmithAttempt(rs);
          blacksmithAttemptList.add(blacksmithAttempt);
        }
        return blacksmithAttemptList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  
  private static BlacksmithAttempt convertRSRowToBlacksmithAttempt(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int goalLevel = rs.getInt(i++);
    boolean guaranteed = rs.getBoolean(i++);
    Date startTime = new Date(rs.getTimestamp(i++).getTime());
    
    int diamondGuaranteeCost = rs.getInt(i++);
    if (rs.wasNull()) diamondGuaranteeCost = ControllerConstants.NOT_SET;
    
    Date timeOfSpeedup = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      timeOfSpeedup = new Date(ts.getTime());
    }
    
    boolean attemptComplete = rs.getBoolean(i++);
    
    return new BlacksmithAttempt(id, userId, equipId, goalLevel, guaranteed, startTime,
        diamondGuaranteeCost, timeOfSpeedup, attemptComplete);
  }
}
