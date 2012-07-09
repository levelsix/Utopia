package com.lvl6.retrieveutils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class AdColonyRecentHistoryRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_ADCOLONY_RECENT_HISTORY;

  public static boolean checkIfDuplicateDigest(String digest) {
    log.debug("checking if digest already exists for digest=" + digest);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.ADCOLONY_RECENT_HISTORY__DIGEST, digest);

    boolean isDuplicateDigest = false;
    
    //Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = null;
    //if (conn != null) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            isDuplicateDigest = true;
          }
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      } 
    //}
    DBConnection.get().close(rs, null);
    return isDuplicateDigest;
  }





}
