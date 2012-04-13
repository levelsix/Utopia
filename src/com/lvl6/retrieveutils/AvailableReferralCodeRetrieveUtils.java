package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class AvailableReferralCodeRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_AVAILABLE_REFERRAL_CODES;

  public static String getAvailableReferralCode() {
    log.debug("getting available referral code");
    
    String query = "select " + DBConstants.AVAILABLE_REFERRAL_CODES__CODE + " from " + TABLE_NAME + 
        " where " + DBConstants.AVAILABLE_REFERRAL_CODES__ID + " >= (select floor( max(" + 
        DBConstants.AVAILABLE_REFERRAL_CODES__ID + ") * rand()) from " + DBConstants.TABLE_AVAILABLE_REFERRAL_CODES
        + ") order by " + DBConstants.AVAILABLE_REFERRAL_CODES__CODE + " limit 1";
    
    

    String availableReferralCode = null;
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectDirectQueryNaive(conn, query, null);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            availableReferralCode = rs.getString(DBConstants.AVAILABLE_REFERRAL_CODES__CODE);
            break;
          }
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      } 
    }
    DBConnection.close(rs, null, conn);

    return availableReferralCode;
  }


}
