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
    log.debug("generating available referral code");
    String availableReferralCode = null;
    
    String query = "select floor(rand()*count(*)) from " + TABLE_NAME;

    Integer offset = null;
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectDirectQueryNaive(conn, query, null);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          while(rs.next()) {
            offset = rs.getInt(1);
            break;
          }
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      } 
    }
    DBConnection.close(rs, null, null);
    
    ResultSet rs2 = null;
    if (conn != null) {
      query = "SELECT " + DBConstants.AVAILABLE_REFERRAL_CODES__CODE+ " FROM " + TABLE_NAME + " LIMIT " + offset + ", 1"; 
      rs2 = DBConnection.selectDirectQueryNaive(conn, query, null);
      if (rs2 != null) {
        try {
          rs2.last();
          rs2.beforeFirst();
          while(rs2.next()) {
            availableReferralCode = rs2.getString(DBConstants.AVAILABLE_REFERRAL_CODES__CODE);
            break;
          }
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      } 
    }
    DBConnection.close(rs2, null, conn);

    return availableReferralCode;
  }


}
