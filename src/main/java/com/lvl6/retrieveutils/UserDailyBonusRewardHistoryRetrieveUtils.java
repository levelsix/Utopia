package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserDailyBonusRewardHistory;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserDailyBonusRewardHistoryRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_DAILY_BONUS_REWARD_HISTORY;
  
  public static UserDailyBonusRewardHistory getLastDailyRewardAwardedForUserId(int userId) { 
    //fetch most recent entry for the daily reward given to the user with id=userId
    log.debug("getting most recent reward awarded to user with id " + userId);
    Map <String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.USER_DAILY_BONUS_REWARD_HISTORY__USER_ID, userId);
    String orderByColumn = DBConstants.USER_DAILY_BONUS_REWARD_HISTORY__DATE_AWARDED;
    int limit = 1;

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(
        conn, absoluteConditionParams, TABLE_NAME, orderByColumn, limit);
    List<UserDailyBonusRewardHistory> rewardList = convertRSToUserDailyBonusRewardHistory(rs);
    DBConnection.get().close(rs, null, conn);
    
    UserDailyBonusRewardHistory returnValue = null;
    if (rewardList.size() == 1) {
      returnValue = rewardList.get(0);
    }
      
    return returnValue;
  }
  
  private static List<UserDailyBonusRewardHistory> convertRSToUserDailyBonusRewardHistory(ResultSet rs) {
    List<UserDailyBonusRewardHistory> udbrhList = new ArrayList<UserDailyBonusRewardHistory>();
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserDailyBonusRewardHistory udbrh = convertRSRowToUserDailyBonusRewardHistory(rs);
          udbrhList.add(udbrh);
        }
        return udbrhList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return udbrhList;
  }
  
  private static UserDailyBonusRewardHistory convertRSRowToUserDailyBonusRewardHistory(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int currencyRewarded = rs.getInt(i++);
    boolean isCoins = rs.getBoolean(i++);
    int boosterPackIdRewarded = rs.getInt(i++);
    if (rs.wasNull()) {
      boosterPackIdRewarded = ControllerConstants.NOT_SET;
    }
    int equipIdRewarded = rs.getInt(i++); //could be null
    if (rs.wasNull()) {
      equipIdRewarded = ControllerConstants.NOT_SET;
    }
    int nthConsecutiveDay = rs.getInt(i++);
    Date dateAwarded = new Date(rs.getTimestamp(i++).getTime()); //should not be null
    
    return new UserDailyBonusRewardHistory(id, userId, currencyRewarded, isCoins, 
        boosterPackIdRewarded, equipIdRewarded, nthConsecutiveDay, dateAwarded);
  }
}
