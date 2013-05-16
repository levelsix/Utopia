package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserLockBoxItem;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.StringUtils;

/*NO UserTask needed because you can just return a map- only two non-user fields*/
@Component @DependsOn("gameServer") public class UserLockBoxItemRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static final String TABLE_NAME = DBConstants.TABLE_USER_LOCK_BOX_ITEMS;
  
  public static Map<Integer, Integer> getLockBoxItemIdsToQuantityForUser(int userId) {
    log.debug("retrieving lock box item ids to num lock boxes map for userId " + userId);
    //TODO:
    //SHOULD JUST GET THE 5 CORRESPONDING TO THE LATEST LOCK BOX EVENT
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    Map<Integer, Integer> lockBoxItemIdsToNumLockBoxes = convertRSToLockBoxItemIdsToNumLockBoxesMap(rs);
    DBConnection.get().close(rs, null, conn);
    return lockBoxItemIdsToNumLockBoxes;
  }
  
  public static Map<Integer, UserLockBoxItem> getLockBoxItemIdsToUserLockBoxItemsForUser(int userId,
      Collection<Integer> lockBoxItemIds) {
    log.debug("retrieving lock box item ids to user lock boxes map for userId " + userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems = null;
    
    if (null == lockBoxItemIds || lockBoxItemIds.isEmpty()) {
      rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    } else {
      List<Object> values = new ArrayList<Object>();
      String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
          DBConstants.USER_LOCK_BOX_ITEMS__USER_ID + "=?";
      values.add(userId);
      query += " AND " + DBConstants.USER_LOCK_BOX_ITEMS__ITEM_ID + " in (";
      
      int amount = lockBoxItemIds.size();
      List<String> questionMarks = Collections.nCopies(amount, "?");
      query += StringUtils.getListInString(questionMarks, ",") + ")";
          
      values.addAll(lockBoxItemIds);
      rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
      
    }
    lockBoxItemIdsToUserLockBoxItems =
        convertRSToLockBoxItemIdsToUserLockBoxItemsMap(rs);
    //log.error("lockBoxItemIdsToUserLockBoxItems=" + lockBoxItemIdsToUserLockBoxItems);
    
    DBConnection.get().close(rs, null, conn);
    return lockBoxItemIdsToUserLockBoxItems;
  }
  
  private static Map<Integer, Integer> convertRSToLockBoxItemIdsToNumLockBoxesMap(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Integer> lockBoxItemIdsToNumLockBoxes = new HashMap<Integer, Integer>();
        while(rs.next()) {  //should only be one
          int i = 1;
          int lockBoxItemId = rs.getInt(i++);
          i++; //Skip user_id column
          int numLockBoxes = rs.getInt(i++);
          lockBoxItemIdsToNumLockBoxes.put(lockBoxItemId, numLockBoxes);
        }
        return lockBoxItemIdsToNumLockBoxes;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }
  
  private static Map<Integer, UserLockBoxItem> convertRSToLockBoxItemIdsToUserLockBoxItemsMap(ResultSet rs) {
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, UserLockBoxItem> lockBoxItemIdsToUserLockBoxItems = new HashMap<Integer, UserLockBoxItem>();
        while(rs.next()) {
          UserLockBoxItem ulbi = convertRSRowToUserLockBoxItem(rs);
          if (null != ulbi) {
            lockBoxItemIdsToUserLockBoxItems.put(ulbi.getLockBoxItemId(), ulbi);
          }
        }
        return lockBoxItemIdsToUserLockBoxItems;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return null;
  }
  
  private static UserLockBoxItem convertRSRowToUserLockBoxItem(ResultSet rs) throws SQLException {
    int i = 1;
    int lockBoxItemId = rs.getInt(i++);
    int userId = Integer.parseInt(rs.getString(i++));
    int quantity = rs.getInt(i++);
    
    UserLockBoxItem ulbi = new UserLockBoxItem(lockBoxItemId, userId, quantity);
    return ulbi;
  }
  
}
