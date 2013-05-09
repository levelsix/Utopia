package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserCityRetrieveUtils {

  private final int NOT_SET = -1;
  
  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private final String TABLE_NAME = DBConstants.TABLE_USER_CITIES;
  
  
  ////@Cacheable(value="cityIdToUserCityRankCache", key="#userId")
  public Map<Integer, Integer> getCityIdToUserCityRank(int userId) {
    log.debug("retrieving city id to user city rank map for userId " + userId);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Integer> cityIdToUserCityRankMap = convertRSToCityIdToCityRankMap(rs);
    DBConnection.get().close(rs, null, conn);
    return cityIdToUserCityRankMap;
  }
  
  
  
  ////@Cacheable(value="currentCityRankForUserCache", key="#userId+':'+#cityId")
  public int getCurrentCityRankForUser(int userId, int cityId) {
    log.debug("retrieving user city info for userId " + userId + " and cityId " + cityId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CITIES__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_CITIES__CITY_ID, cityId);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    int cityRank = convertRSToCityRank(rs);
    DBConnection.get().close(rs, null, conn);
    return cityRank;
  }
  
  private int convertRSToCityRank(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          int currentRank = rs.getInt(DBConstants.USER_CITIES__CURRENT_RANK);
          return currentRank;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return NOT_SET;
  }

  private Map<Integer, Integer> convertRSToCityIdToCityRankMap(ResultSet rs) {
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
        log.error("problem with database call.", e);
        
      }
    }
    return cityIdToCityRankMap;
  }
  
}
