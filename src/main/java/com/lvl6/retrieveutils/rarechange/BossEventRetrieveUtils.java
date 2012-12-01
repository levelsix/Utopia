package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BossEvent;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BossEventRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<BossEvent>> bossIdsToBossEvents;
  private static Map<Integer, BossEvent> idsToBossEvents;

  private static final String TABLE_NAME = DBConstants.TABLE_BOSS_EVENTS;

  public static Map<Integer, BossEvent> getIdsToBossEvents() {
    log.debug("retrieving all boss events data map");
    if (idsToBossEvents == null) {
      setStaticIdsToBossEvents();
    }
    return idsToBossEvents;
  }

  public static BossEvent getBossEventForId(int id) {
    log.debug("retrieve bossEvent data for bossEventId " + id);
    if (idsToBossEvents == null) {
      setStaticIdsToBossEvents();      
    }
    return idsToBossEvents.get(id);
  }

  public static Map<Integer, BossEvent> getBossEventsForIds(List<Integer> ids) {
    log.debug("retrieve bossEvent data for ids " + ids);
    if (idsToBossEvents == null) {
      setStaticIdsToBossEvents();      
    }
    Map<Integer, BossEvent> toreturn = new HashMap<Integer, BossEvent>();
    for (Integer id : ids) {
      toreturn.put(id,  idsToBossEvents.get(id));
    }
    return toreturn;
  }

  public static List<BossEvent> getAllBossEventsForBossId(int bossId) {
    log.debug("retrieving all bossEvents for bossId " + bossId);
    if (bossIdsToBossEvents == null) {
      setStaticBossIdsToBossEvents();
    }
    return bossIdsToBossEvents.get(bossId);
  }

  private static void setStaticBossIdsToBossEvents() {
    log.debug("setting static map of bossId to bossEvents");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map<Integer, List<BossEvent>> bossIdsToBossEventsTemp = new HashMap<Integer, List<BossEvent>>();
          while(rs.next()) {  
            BossEvent be = convertRSRowToBossEvent(rs);
            if (be != null) {
              int bid = be.getBossId();
              if (bossIdsToBossEventsTemp.get(bid) == null) {
            	  bossIdsToBossEventsTemp.put(bid, new ArrayList<BossEvent>());
              }
              bossIdsToBossEventsTemp.get(bid).add(be);
            }
          }
          bossIdsToBossEvents = bossIdsToBossEventsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  private static void setStaticIdsToBossEvents() {
    log.debug("setting static map of ids to bossEvents");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, BossEvent> idsToBossEventsTemp = new HashMap<Integer, BossEvent>();
          while(rs.next()) {  //should only be one
            BossEvent be = convertRSRowToBossEvent(rs);
            if (be != null)
            	idsToBossEventsTemp.put(be.getId(), be);
          }
          idsToBossEvents = idsToBossEventsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticBossIdsToBossEvents();
    setStaticIdsToBossEvents();
  }

  /*
   * assumes the resultset is appropriately set up. traverses the row it's on.
   */
  private static BossEvent convertRSRowToBossEvent(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int bossId = rs.getInt(i++);
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    Date endDate = new Date(rs.getTimestamp(i++).getTime());
    String bossImageName = rs.getString(i++);
    String eventName = rs.getString(i++);
    
    BossEvent be = new BossEvent(id, bossId, startDate, endDate, bossImageName, eventName);
    return be;
  }
}
