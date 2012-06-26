package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.PlayerWallPost;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class PlayerWallPostRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_PLAYER_WALL_POSTS;

  public static PlayerWallPost getSpecificActivePlayerWallPost(int wallPostId) {
    log.debug("retrieving wall post with id " + wallPostId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsById(wallPostId, TABLE_NAME);
    PlayerWallPost playerWallPost = convertRSToSinglePlayerWallPost(rs);
    DBConnection.get().close(rs, null, conn);
    return playerWallPost;
  }

//  public static List<PlayerWallPost> getAllPlayerWallPostsAfterLastlogoutForWallOwner(Timestamp lastLogout, int wallOwnerId) {
//    log.debug("retrieving all wall posts for " + wallOwnerId + " after " + lastLogout);
//    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
//    absoluteParams.put(DBConstants.PLAYER_WALL_POSTS__WALL_OWNER_ID, wallOwnerId);
//
//    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
//    greaterThanParams.put(DBConstants.PLAYER_WALL_POSTS__TIME_OF_POST, lastLogout);
//    
//    Connection conn = DBConnection.get().connectionManager.get();
//    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.PLAYER_WALL_POSTS__TIME_OF_POST, greaterThanParams);
//    List<PlayerWallPost> playerWallPosts = convertRSToPlayerWallPosts(rs);
//    DBConnection.get().close(rs, null, conn);
//    return playerWallPosts;
//  }
  
  public static List<PlayerWallPost> getMostRecentActivePlayerWallPostsForPlayerBeforePostId(int limit, int postId, int wallOwnerId) {
    log.debug("retrieving " + limit + " player wall posts before certain postId " + postId + " for wallOwner " + wallOwnerId);
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.PLAYER_WALL_POSTS__ID, postId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.PLAYER_WALL_POSTS__WALL_OWNER_ID, wallOwnerId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(absoluteParams, TABLE_NAME, DBConstants.PLAYER_WALL_POSTS__ID, limit, lessThanParamsToVals);
    List<PlayerWallPost> playerWallPosts = convertRSToPlayerWallPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return playerWallPosts;
  }
  
  public static List<PlayerWallPost> getMostRecentPlayerWallPostsForWallOwner(int limit, int wallOwnerId) {
    log.debug("retrieving " + limit + " player wall posts for wallOwner " + wallOwnerId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.PLAYER_WALL_POSTS__WALL_OWNER_ID, wallOwnerId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(absoluteParams, TABLE_NAME, DBConstants.PLAYER_WALL_POSTS__ID, limit);
    List<PlayerWallPost> playerWallPosts = convertRSToPlayerWallPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return playerWallPosts;
  }
  
  private static List<PlayerWallPost> convertRSToPlayerWallPosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<PlayerWallPost> wallPosts = new ArrayList<PlayerWallPost>();
        while(rs.next()) {
          PlayerWallPost pwp = convertRSRowToPlayerWallPost(rs);
          if (pwp != null) wallPosts.add(pwp);
        }
        return wallPosts;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static PlayerWallPost convertRSToSinglePlayerWallPost(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          PlayerWallPost pwp = convertRSRowToPlayerWallPost(rs);
          return pwp;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static PlayerWallPost convertRSRowToPlayerWallPost(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int wallOwnerId = rs.getInt(i++);
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    String content = rs.getString(i++);

    PlayerWallPost pwp = new PlayerWallPost(id, posterId, wallOwnerId, timeOfPost, content);
  
    return pwp;
  }
}
