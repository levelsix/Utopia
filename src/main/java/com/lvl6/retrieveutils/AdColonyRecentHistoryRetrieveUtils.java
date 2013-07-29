package com.lvl6.retrieveutils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class AdColonyRecentHistoryRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_ADCOLONY_RECENT_HISTORY;

  public static boolean checkIfDuplicateDigest(String digest) {
    log.debug("checking if digest already exists for digest=" + digest);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIGEST, digest);

    boolean isDuplicateDigest = false;
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            isDuplicateDigest = true;
          }
        } catch (SQLException e) {
          log.error("problem with database call.", e);
        }
      } 
    }
    DBConnection.get().close(rs, null, conn);
    return isDuplicateDigest;
  }





}
