package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class StartupNoticesToPlayersRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static List<String> notices;

  private static final String TABLE_NAME = DBConstants.TABLE_STARTUP_NOTICES_TO_PLAYERS;

  public static List<String> getAllActiveNotices() {
    log.debug("retrieving all notices placed in a set");
    if (notices == null) {
      setStaticActiveNotices();
    }
    return notices;
  }

  private static void setStaticActiveNotices() {
    log.debug("setting static Set of notices");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
      absoluteConditionParams.put(DBConstants.STARTUP_NOTICES_TO_PLAYERS__IS_ACTIVE, true);
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, TABLE_NAME);
      //rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          List<String> noticesTemp = new ArrayList<String>();
          while(rs.next()) { 
            String noticesTerm = convertRSRowToNotices(rs);
            if (null != noticesTerm)
              noticesTemp.add(noticesTerm);
          }
          notices = noticesTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticActiveNotices();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static String convertRSRowToNotices(ResultSet rs) throws SQLException {
    int i = 1;
    String noticesTerm = rs.getString(i++);
    
    return noticesTerm;
  }
}
