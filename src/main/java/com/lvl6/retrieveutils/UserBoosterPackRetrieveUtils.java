package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserBoosterPackRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_BOOSTER_PACK_HISTORY;
  
  public static int getNumPacksPurchasedAfterDateForUserAndPackId(int userId, int packId, Timestamp now) {
    log.debug("retrieving number of booster packs purchased after " + now  
        +  "for userId " + userId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    List<String> columns = new ArrayList<String>();
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    String orderByColumn = null;
    Map<String, Object> greaterThanConditionParams = new HashMap<String, Object>();
    
    columns.add(DBConstants.USER_BOOSTER_PACK_HISTORY__NUM_BOUGHT);
    absoluteConditionParams.put(
        DBConstants.USER_BOOSTER_PACK_HISTORY__USER_ID, userId);
    absoluteConditionParams.put(
        DBConstants.USER_BOOSTER_PACK_HISTORY__BOOSTER_PACK_ID, packId);
    greaterThanConditionParams.put(
        DBConstants.USER_BOOSTER_PACK_HISTORY__TIME_OF_PURCHASE,
        now);
    
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescGreaterthan(
        conn, absoluteConditionParams, TABLE_NAME, orderByColumn, 
        greaterThanConditionParams);
    int quantityBought = convertRSToNumPurchased(rs);
    DBConnection.get().close(rs, null, conn);
    return quantityBought;
  }
  
  private static int convertRSToNumPurchased(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        int numPurchased = 0;
        while(rs.next()) {
          int numBought = rs.getInt(DBConstants.USER_BOOSTER_PACK_HISTORY__NUM_BOUGHT);
          numPurchased += numBought;
        }
        return numPurchased;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return 0;
  }
  
}
