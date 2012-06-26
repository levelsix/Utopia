package com.lvl6.retrieveutils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class IAPHistoryRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_IAP_HISTORY;

  public static boolean checkIfDuplicateTransaction(long transactionId) {
    log.debug("checking if transaction already exists for transaction Id" + transactionId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.IAP_HISTORY__TRANSACTION_ID, transactionId);

    boolean isDuplicateTransaction = false;
    
    //Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = null;
    //if (conn != null) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            isDuplicateTransaction = true;
          }
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      } 
    //}
    DBConnection.get().close(rs, null);
    return isDuplicateTransaction;
  }





}
