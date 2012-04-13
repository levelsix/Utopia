package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.Referral;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class ReferralsRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_REFERRALS;

  public static List<Referral> getAllReferralsAfterLastlogoutForReferrer(Timestamp lastLogout, int referrerId) {
    log.debug("retrieving all referrals for referrer " + referrerId + " after " + lastLogout);
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.REFERRALS__REFERRER_ID, referrerId);

    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
    greaterThanParams.put(DBConstants.REFERRALS__TIME_OF_REFERRAL, lastLogout);
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.REFERRALS__TIME_OF_REFERRAL, greaterThanParams);
    List<Referral> referrals = convertRSToReferralsList(rs);
    DBConnection.close(rs, null, conn);
    return referrals;
  }
  
  private static List<Referral> convertRSToReferralsList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<Referral> referralsList = new ArrayList<Referral>();
        while(rs.next()) {
          Referral referral = convertRSRowToReferral(rs);
          referralsList.add(referral);
        }
        return referralsList;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static Referral convertRSRowToReferral(ResultSet rs) throws SQLException {
    int i = 1;
    int referrerId = rs.getInt(i++);
    int newlyReferredId = rs.getInt(i++);
    Date timeOfReferral = new Date(rs.getTimestamp(i++).getTime());
    return new Referral(referrerId, newlyReferredId, timeOfReferral);
  }
}
