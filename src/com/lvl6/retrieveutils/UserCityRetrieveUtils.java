package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
public class UserCityRetrieveUtils {

  private static final int NOT_SET = -1;
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_CITIES;
  
  public static int getCurrentCityRankForUser(int userId, int cityId) {
    log.info("retrieving user city info for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CITIES__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_CITIES__CITY_ID, cityId);
    return convertRSToCityRank(DBConnection.selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME));
  }
  
  private static int convertRSToCityRank(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          int currentRank = rs.getInt(DBConstants.USER_CITIES__CURRENT_RANK);
          return currentRank;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return NOT_SET;
  }
  
}
