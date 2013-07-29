package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Referral;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ReferralsRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_REFERRALS;

  //TODO: when we actually impl referrals, dont base it on last logout. get the most recent ones. like BattleDetails
  public static List<Referral> getAllReferralsAfterLastlogoutForReferrer(Timestamp lastLogout, int referrerId) {
    log.debug("retrieving all referrals for referrer " + referrerId + " after " + lastLogout);
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.REFERRALS__REFERRER_ID, referrerId);

    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
    greaterThanParams.put(DBConstants.REFERRALS__TIME_OF_REFERRAL, lastLogout);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.REFERRALS__TIME_OF_REFERRAL, greaterThanParams);
    List<Referral> referrals = convertRSToReferralsList(rs);
    DBConnection.get().close(rs, null, conn);
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
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static Referral convertRSRowToReferral(ResultSet rs) throws SQLException {
    int i = 1;
    int referrerId = rs.getInt(i++);
    int newlyReferredId = rs.getInt(i++);
    Date timeOfReferral = new Date(rs.getTimestamp(i++).getTime());
    int coinsGivenToReferrer = rs.getInt(i++);
    return new Referral(referrerId, newlyReferredId, timeOfReferral, coinsGivenToReferrer);
  }
}
