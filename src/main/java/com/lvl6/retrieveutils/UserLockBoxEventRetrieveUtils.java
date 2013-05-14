package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserLockBoxEvent;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserLockBoxEventRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_LOCK_BOX_EVENTS;
  
  public static Map<Integer, UserLockBoxEvent> getLockBoxEventIdsToLockBoxEventsForUser(int userId) {
    log.debug("retrieving lock box event ids to num lock boxes map for userId " + userId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, UserLockBoxEvent> lockBoxEventIdsToUserLockBoxEvents = grabUserLockBoxEventsFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return lockBoxEventIdsToUserLockBoxEvents;
  }
  
  public static UserLockBoxEvent getUserLockBoxEventForUserAndEventId(int userId, int lockBoxEventId) {
    log.debug("retrieving num lock boxes map for userId " + userId + " lockBoxEventId " + lockBoxEventId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_LOCK_BOX_EVENTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_LOCK_BOX_EVENTS__EVENT_ID, lockBoxEventId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserLockBoxEvent event = grabUserLockBoxEventFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return event;
  }

  private static UserLockBoxEvent grabUserLockBoxEventFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserLockBoxEvent uc = convertRSRowToUserLockBoxEvent(rs);
          return uc;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static Map<Integer, UserLockBoxEvent> grabUserLockBoxEventsFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, UserLockBoxEvent> userEvents = new TreeMap<Integer, UserLockBoxEvent>();
        while(rs.next()) {
          UserLockBoxEvent uc = convertRSRowToUserLockBoxEvent(rs);
          userEvents.put(uc.getLockBoxId(), uc);
        }
        return userEvents;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }
  
  private static UserLockBoxEvent convertRSRowToUserLockBoxEvent(ResultSet rs) throws SQLException {
    int i = 1;
    int lockBoxId = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int numBoxes = rs.getInt(i++);

    Date lastPickTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastPickTime = new Date(ts.getTime());
    }
    
    int numTimesCompleted = rs.getInt(i++);
    boolean hasBeenRedeemed = rs.getBoolean(i++);
    
    UserLockBoxEvent event = new UserLockBoxEvent(lockBoxId, userId, numBoxes,
        numTimesCompleted, lastPickTime, hasBeenRedeemed);
    return event;
  }
}
