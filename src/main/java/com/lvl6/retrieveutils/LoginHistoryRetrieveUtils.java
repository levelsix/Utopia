package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class LoginHistoryRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_LOGIN_HISTORY;
  
  public static boolean userLoggedInByUDID(String UDID) { 
    log.debug("login check for user with udid " + UDID);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__UDID, UDID);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    boolean loggedIn = convertRSToBoolean(rs);
    DBConnection.get().close(rs, null, conn);
    return loggedIn;
  }
  
  private static boolean convertRSToBoolean(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        if(rs.next()) {  //user logged in
          return true;
        }
        return false;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return false;
  }
  
}
