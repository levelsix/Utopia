package com.lvl6.retrieveutils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class IAPHistoryRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_IAP_HISTORY;
  
  public static boolean checkIfDuplicateTransaction(long transactionId) {
    log.info("checking if transaction already exists");
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.IAP_HISTORY__TRANSACTION_ID, transactionId);
    ResultSet rs = DBConnection.selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          return true;
        }
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    } 
    return false;
  }
}
