package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserBoosterItemRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_BOOSTER_ITEMS;
  
  public static Map<Integer, Integer> getBoosterItemIdsToQuantityForUser(int userId) {
    log.debug("retrieving booster item ids to num booster items map for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Integer> boosterItemIdsToNumReceived = convertRSToBoosterItemIdsToNumReceivedMap(rs);
    DBConnection.get().close(rs, null, conn);
    return boosterItemIdsToNumReceived;
  }
  
  private static Map<Integer, Integer> convertRSToBoosterItemIdsToNumReceivedMap(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Integer> boosterItemIdsToNumReceived = new HashMap<Integer, Integer>();
        while(rs.next()) {  //should only be one
          int i = 1;
          int boosterItemId = rs.getInt(i++);
          i++; //Skip user_id column
          int numReceived = rs.getInt(i++);
          boosterItemIdsToNumReceived.put(boosterItemId, numReceived);
        }
        return boosterItemIdsToNumReceived;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }
  
}
