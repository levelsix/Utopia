package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.LockBoxEvent;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class LockBoxEventRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, LockBoxEvent> lockBoxEventIdsToLockBoxEvents;

  private static final String TABLE_NAME = DBConstants.TABLE_LOCK_BOX_EVENTS;

  public static Map<Integer, LockBoxEvent> getLockBoxEventIdsToLockBoxEvents() {
    log.debug("retrieving all lockBoxEvents data map");
    if (lockBoxEventIdsToLockBoxEvents == null) {
      setStaticLockBoxEventIdsToLockBoxEvents();
    }
    return lockBoxEventIdsToLockBoxEvents;
  }

  public static LockBoxEvent getLockBoxEventForLockBoxEventId(int lockBoxEventId) {
    log.debug("retrieve lockBoxEvent data for lockBoxEvent " + lockBoxEventId);
    if (lockBoxEventIdsToLockBoxEvents == null) {
      setStaticLockBoxEventIdsToLockBoxEvents();      
    }
    return lockBoxEventIdsToLockBoxEvents.get(lockBoxEventId);
  }

  public static Map<Integer, LockBoxEvent> getLockBoxEventsForLockBoxEventIds(List<Integer> ids) {
    log.debug("retrieve lockBoxEvent data for lockBoxEventids " + ids);
    if (lockBoxEventIdsToLockBoxEvents == null) {
      setStaticLockBoxEventIdsToLockBoxEvents();      
    }
    Map<Integer, LockBoxEvent> toreturn = new HashMap<Integer, LockBoxEvent>();
    for (Integer id : ids) {
      toreturn.put(id,  lockBoxEventIdsToLockBoxEvents.get(id));
    }
    return toreturn;
  }

  private static void setStaticLockBoxEventIdsToLockBoxEvents() {
    log.debug("setting static map of lockBoxEventIds to lockBoxEvents");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, LockBoxEvent> lockBoxEventIdsToLockBoxEventsTemp = new HashMap<Integer, LockBoxEvent>();
          while(rs.next()) {  //should only be one
            LockBoxEvent lockBoxEvent = convertRSRowToLockBoxEvent(rs);
            if (lockBoxEvent != null)
              lockBoxEventIdsToLockBoxEventsTemp.put(lockBoxEvent.getId(), lockBoxEvent);
          }
          lockBoxEventIdsToLockBoxEvents = lockBoxEventIdsToLockBoxEventsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticLockBoxEventIdsToLockBoxEvents();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static LockBoxEvent convertRSRowToLockBoxEvent(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    Date endDate = new Date(rs.getTimestamp(i++).getTime());
    String lockBoxImageName = rs.getString(i++);
    String eventName = rs.getString(i++);
    int prizeEquipId = rs.getInt(i++);
    String descriptionString = rs.getString(i++);
    String descriptionImageName = rs.getString(i++);
    String tagImageName = rs.getString(i++);
    
    LockBoxEvent lockBoxEvent = new LockBoxEvent(id, startDate, endDate, lockBoxImageName, eventName, prizeEquipId, descriptionString, descriptionImageName, tagImageName);
    return lockBoxEvent;
  }
}
