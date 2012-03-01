package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class AvailableReferralCodeRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_AVAILABLE_REFERRAL_CODES;
  
  public static String getAvailableReferralCode() {
    log.info("getting available referral code");
    String query = "select " + DBConstants.AVAILABLE_REFERRAL_CODES__CODE + " from " + TABLE_NAME + 
        " limit 0, 1";
    ResultSet rs = DBConnection.selectDirectQueryNaive(query, null);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          return rs.getString(DBConstants.AVAILABLE_REFERRAL_CODES__CODE);
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    } 
    return null;
  }
  

}
