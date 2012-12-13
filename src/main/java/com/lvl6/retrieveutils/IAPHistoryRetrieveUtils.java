package com.lvl6.retrieveutils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class IAPHistoryRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_IAP_HISTORY;

  public static boolean checkIfDuplicateTransaction(long transactionId) {
    log.debug("checking if transaction already exists for transaction Id" + transactionId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.IAP_HISTORY__TRANSACTION_ID, transactionId);

    boolean isDuplicateTransaction = false;
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            isDuplicateTransaction = true;
          }
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      } 
    }
    DBConnection.get().close(rs, null, conn);
    return isDuplicateTransaction;
  }

  public static boolean checkIfUserHasPurchased(int userId) {
    log.debug("checking if player has purchased anything");
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.IAP_HISTORY__USER_ID, userId);

    boolean hasBought = false;
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            hasBought = true;
            break;
          }
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      } 
    }
    DBConnection.get().close(rs, null, conn);
    return hasBought;
  }




}
