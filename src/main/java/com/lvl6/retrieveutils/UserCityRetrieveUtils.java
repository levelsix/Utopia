package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
public class UserCityRetrieveUtils {

  private static final int NOT_SET = -1;
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_CITIES;
  
  public static Map<Integer, Integer> getCityIdToUserCityRank(int userId) {
    log.debug("retrieving city id to user city rank map for userId " + userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Integer> cityIdToUserCityRankMap = convertRSToCityIdToCityRankMap(rs);
    DBConnection.get().close(rs, null, conn);
    return cityIdToUserCityRankMap;
  }
  
  public static int getCurrentCityRankForUser(int userId, int cityId) {
    log.debug("retrieving user city info for userId " + userId + " and cityId " + cityId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CITIES__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_CITIES__CITY_ID, cityId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    int cityRank = convertRSToCityRank(rs);
    DBConnection.get().close(rs, null, conn);
    return cityRank;
  }
  
  private static int convertRSToCityRank(ResultSet rs) {
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

  private static Map<Integer, Integer> convertRSToCityIdToCityRankMap(ResultSet rs) {
    Map<Integer, Integer> cityIdToCityRankMap = new HashMap<Integer, Integer>();
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          int cityId = rs.getInt(DBConstants.USER_CITIES__CITY_ID);
          int currentRank = rs.getInt(DBConstants.USER_CITIES__CURRENT_RANK);
          cityIdToCityRankMap.put(cityId, currentRank);
        }
        return cityIdToCityRankMap;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return cityIdToCityRankMap;
  }
  
}
