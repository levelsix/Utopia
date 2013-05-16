package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BossEvent;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BossEventRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticIdsToBossEvents();
  }

  /*
   * assumes the resultset is appropriately set up. traverses the row it's on.
   */
  private static BossEvent convertRSRowToBossEvent(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int cityId = rs.getInt(i++);
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    Date endDate = new Date(rs.getTimestamp(i++).getTime());
    String eventName = rs.getString(i++);
    String headerImage = rs.getString(i++);
    int leftEquipId = rs.getInt(i++);
    String leftTag = rs.getString(i++);
    int rightEquipId = rs.getInt(i++);
    String rightTag = rs.getString(i++);
    int middleEquipId = rs.getInt(i++);
    String middleTag = rs.getString(i++);
    String infoDescription = rs.getString(i++);
    
    BossEvent be = new BossEvent(id, cityId, startDate, endDate, eventName, headerImage, leftEquipId, leftTag, middleEquipId, middleTag, rightEquipId, rightTag, infoDescription);
    return be;
  }
}
