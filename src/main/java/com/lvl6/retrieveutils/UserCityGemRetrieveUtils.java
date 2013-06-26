package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserCityGem;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;
@Component @DependsOn("gameServer") public class UserCityGemRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_CITY_GEMS;
  
  public static Map<Integer, UserCityGem> getGemIdsToGemsForUserAndCity(
      int userId, int cityId) {
    log.debug("retrieving gem ids to gems map for userId " + userId +
        " and cityId " + cityId);
    Connection conn = DBConnection.get().getConnection();
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.USER_CITY_GEMS__USER_ID, userId);
    absoluteConditionParams.put(DBConstants.USER_CITY_GEMS__CITY_ID, cityId);
    
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(
        conn, absoluteConditionParams, TABLE_NAME);
    Map<Integer, UserCityGem> cityGemIdsToNumLockBoxes =
        convertRSToGemIdsToGemsForUserAndCity(rs);
    DBConnection.get().close(rs, null, conn);
    return cityGemIdsToNumLockBoxes;
  }
  
  public static int numCityGemsForUser(int userId) {
    
    String query = 
        "SELECT COUNT(*) " +
    		"FROM kingdom.user_city_gems ucg " +
    		"WHERE ucg.user_city_gems = ? ";
    
    List<Object> values = new ArrayList<Object>();
    values.add(userId);
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    int numGems = convertRSToNumGemsCollected(rs);
    DBConnection.get().close(rs, null, conn);
    return numGems;
  }
  
  private static Map<Integer, UserCityGem> convertRSToGemIdsToGemsForUserAndCity(ResultSet rs) {
    Map<Integer, UserCityGem> cityGemIdsToUserCityGems = new HashMap<Integer, UserCityGem>();
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserCityGem ucg = convertRSRowToUserCityGem(rs);
          if (null != ucg) {
            cityGemIdsToUserCityGems.put(ucg.getGemId(), ucg);
          }
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return cityGemIdsToUserCityGems;
  }
  
  private static int convertRSToNumGemsCollected(ResultSet rs) {
    int count = 0;
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        while (rs.next()) {
          int i = 1;
          count = rs.getInt(i++);
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return count;
  }
  
  private static UserCityGem convertRSRowToUserCityGem(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int cityId = rs.getInt(i++);
    int gemId = rs.getInt(i++);
    int quantity = rs.getInt(i++);
    
    UserCityGem ucg = new UserCityGem(userId, cityId, gemId, quantity);
    return ucg;
  }
  
}
