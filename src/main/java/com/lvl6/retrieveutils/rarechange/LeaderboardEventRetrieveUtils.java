package com.lvl6.retrieveutils.rarechange;

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

import com.lvl6.info.LeaderboardEvent;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class LeaderboardEventRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, LeaderboardEvent> idsToLeaderBoardEvents;

  private static final String TABLE_NAME = DBConstants.TABLE_LEADERBOARD_EVENTS;

  public static Map<Integer, LeaderboardEvent> getIdsToLeaderboardEvents() {
    log.debug("retrieving leaderboard event data");
    if (idsToLeaderBoardEvents == null) {
      setStaticIdsToLeaderboardEvents();
    }
    return idsToLeaderBoardEvents;
  }

  public static Map<Integer, LeaderboardEvent> getLeaderboardEventsForIds(List<Integer> ids) {
    log.debug("retrieving LeaderboardEvents with ids " + ids);
    if (idsToLeaderBoardEvents == null) {
      setStaticIdsToLeaderboardEvents();
    }
    Map<Integer, LeaderboardEvent> toreturn = new HashMap<Integer, LeaderboardEvent>();
    for (Integer id : ids) {
      toreturn.put(id,  idsToLeaderBoardEvents.get(id));
    }
    return toreturn;
  }

  public static LeaderboardEvent getLeaderboardEventForId(int id) {
    log.debug("retrieving LeaderboardEvent for id " + id);
    if (idsToLeaderBoardEvents == null) {
      setStaticIdsToLeaderboardEvents();
    }
    return idsToLeaderBoardEvents.get(id);
  }

  public static List<LeaderboardEvent> getActiveLeaderboardEvents() {
    long curTime = (new Date()).getTime();
    String now = "\"" + new Timestamp(curTime) + "\"";

    List<LeaderboardEvent> toReturn = new ArrayList<LeaderboardEvent>();
    
    if(null != idsToLeaderBoardEvents) {
      //go through local copy of db, instead of going to db
      for(LeaderboardEvent e : idsToLeaderBoardEvents.values()) {
        if(e.getEndDate().getTime() > curTime && curTime >= e.getStartDate().getTime()) {
          toReturn.add(e);
        }
      }
    } else {
      //initialization crap
      Connection conn = DBConnection.get().getConnection();
      ResultSet rs = null;
      List<String> columns = null;
      Map<String, Object> absoluteConditionParams = null;
      Map<String, Object> relativeGreaterThanConditionParams = new HashMap<String, Object>();
      Map<String, Object> relativeLessThanConditionParams = new HashMap<String, Object>();
      Map<String, Object> likeCondParams = null;
      String conddelim = ",";
      String orderByColumn = "";
      boolean orderByAsc = false;
      int limit = -1; //SELECT_LIMIT_NOT_SET;
      boolean random = false;
      //end initialization
      
      //event should have end time after now
      relativeGreaterThanConditionParams.put(DBConstants.LEADERBOARD_EVENTS__END_TIME, now);
      //event should have start time before now
      relativeLessThanConditionParams.put(DBConstants.LEADERBOARD_EVENTS__START_TIME, now);
      if (null != conn) {
        rs = DBConnection.get().selectRows(conn, columns, absoluteConditionParams, 
            relativeGreaterThanConditionParams, relativeLessThanConditionParams, likeCondParams, 
            TABLE_NAME, conddelim, orderByColumn, orderByAsc, limit, random);
        if (null != rs) {
          try {
            rs.last();
            rs.beforeFirst();
            while(rs.next()) {
              LeaderboardEvent le = convertRSRowToLeaderboardEvent(rs);
              if(null != le) {
                toReturn.add(le);
              }
            }
          } catch (SQLException e) {
            log.error("problem with leaderboard event db call.", e);
          }
        }
      }
      DBConnection.get().close(rs, null, conn);
    }
    
    return toReturn;
  }
  
  private static void setStaticIdsToLeaderboardEvents() {
    log.debug("setting static map of upgrade struct job id to upgrade struct job");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, LeaderboardEvent> idsToLeaderboardEventTemp = new HashMap<Integer, LeaderboardEvent>();
          while(rs.next()) {  //should only be one
            LeaderboardEvent le = convertRSRowToLeaderboardEvent(rs);
            if (le != null)
              idsToLeaderboardEventTemp.put(le.getId(), le);
          }
          idsToLeaderBoardEvents = idsToLeaderboardEventTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs,  null, conn);
  }

  public static void reload() {
    setStaticIdsToLeaderboardEvents();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static LeaderboardEvent convertRSRowToLeaderboardEvent(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    Date endDate = new Date(rs.getTimestamp(i++).getTime());
    String eventName = rs.getString(i++);
    return new LeaderboardEvent(id, startDate, endDate, eventName);
  }
}
