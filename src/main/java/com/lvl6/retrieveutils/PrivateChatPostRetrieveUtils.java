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

import com.lvl6.info.PrivateChatPost;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class PrivateChatPostRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_PRIVATE_CHAT_POSTS;

  
  public static List<PrivateChatPost> getPrivateChatPostsBetweenUsersBeforePostId(
      int limit, int postId, int userOne, int userTwo) {
    log.info("retrieving " + limit + " private chat posts before certain postId "
      + postId + " for userOne " + userOne + " and userTwo" + userTwo);
    
    Connection conn = DBConnection.get().getConnection();
    String query = "";
    List<Object> values = new ArrayList<Object>();
    query += 
        "SELECT * " +
    		"FROM " + TABLE_NAME + " " +
    		"WHERE " + DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID + " IN (?,?) ";
    values.add(userOne);
    values.add(userTwo);
    query +=
        "AND " + DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID + " IN (?,?) ";
    values.add(userOne);
    values.add(userTwo);
    
    //in case no before post id is specified
    if (ControllerConstants.NOT_SET != postId) {
      query += "AND " + DBConstants.PRIVATE_CHAT_POSTS__ID + " < ? ";
      values.add(postId);
    }
    
    query += "ORDER BY " + DBConstants.PRIVATE_CHAT_POSTS__ID + " DESC  LIMIT ?";
    values.add(limit);
    
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    
    List<PrivateChatPost> privateChatPosts = convertRSToPrivateChatPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return privateChatPosts;
  }
  
  public static Map<Integer, PrivateChatPost> getMostRecentPrivateChatPostsByOrToUser(
      int userId, boolean isRecipient) {
    log.debug("retrieving most recent private chat posts. userId "
      + userId + " isRecipient=" + isRecipient);
    
    String otherPersonColumn = null;
    String column = null;
    
    if (isRecipient) {
      otherPersonColumn = DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID;
      column = DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID;
    } else {
      otherPersonColumn = DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID;
      column = DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID;
    }
    Connection conn = DBConnection.get().getConnection();
    List<Object> values = new ArrayList<Object>();
    String query = "";
    String subquery = "";

    //get last post id between specified user and person said user chatted with
    subquery +=
        "(SELECT max(" + DBConstants.PRIVATE_CHAT_POSTS__ID + ") as id " + 
        "FROM " + TABLE_NAME + " " +
        "WHERE " + column + "=? " +
        "GROUP BY " + otherPersonColumn + ")";
    values.add(userId);
    
    //get the actual posts to those ids
    query +=
        "SELECT pcp.*  " +
        "FROM " + subquery + " as idList "
          + "LEFT JOIN " + 
                  TABLE_NAME + " as pcp " +
            "ON idList.id=pcp.id";
    
    
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    
    Map<Integer, PrivateChatPost> privateChatPosts = convertRSToMapIdToPrivateChatPost(rs);
    return privateChatPosts;
  }
  
  
  private static List<PrivateChatPost> convertRSToPrivateChatPosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<PrivateChatPost> wallPosts = new ArrayList<PrivateChatPost>();
        while(rs.next()) {
          PrivateChatPost pwp = convertRSRowToPrivateChatPost(rs);
          if (pwp != null) wallPosts.add(pwp);
        }
        return wallPosts;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static Map<Integer, PrivateChatPost> convertRSToMapIdToPrivateChatPost(ResultSet rs) {
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, PrivateChatPost> idsToPrivateChatPosts =
            new HashMap<Integer, PrivateChatPost>();
        
        while(rs.next()) {
          PrivateChatPost pcp = convertRSRowToPrivateChatPost(rs);
          if (null != pcp) {
            int id = pcp.getId();
            idsToPrivateChatPosts.put(id, pcp);
          }
        }
        
        return idsToPrivateChatPosts;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return null;
  }

  private static PrivateChatPost convertRSRowToPrivateChatPost(ResultSet rs)
      throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int recipientId = rs.getInt(i++);
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    String content = rs.getString(i++);

    PrivateChatPost pwp = new PrivateChatPost(id, posterId, recipientId,
        timeOfPost, content);
  
    return pwp;
  }
  
}
