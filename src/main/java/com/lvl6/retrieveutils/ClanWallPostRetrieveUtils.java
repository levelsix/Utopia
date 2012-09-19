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

import com.lvl6.info.ClanWallPost;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanWallPostRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_WALL_POSTS;

  public static ClanWallPost getSpecificActiveClanWallPost(int wallPostId) {
    log.debug("retrieving wall post with id " + wallPostId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, wallPostId, TABLE_NAME);
    ClanWallPost clanWallPost = convertRSToSingleClanWallPost(rs);
    DBConnection.get().close(rs, null, conn);
    return clanWallPost;
  }

  public static List<ClanWallPost> getMostRecentActiveClanWallPostsForClanBeforePostId(int limit, int postId, int clanId) {
    log.debug("retrieving " + limit + " player wall posts before certain postId " + postId + " for clan " + clanId);
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.CLAN_WALL_POSTS__ID, postId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_WALL_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, absoluteParams, TABLE_NAME, DBConstants.CLAN_WALL_POSTS__ID, limit, lessThanParamsToVals);
    List<ClanWallPost> clanWallPosts = convertRSToClanWallPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanWallPosts;
  }
  
  public static List<ClanWallPost> getMostRecentClanWallPostsForClan(int limit, int clanId) {
    log.debug("retrieving " + limit + " clan wall posts for clan " + clanId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_WALL_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParams, TABLE_NAME, DBConstants.PLAYER_WALL_POSTS__ID, limit);
    List<ClanWallPost> clanWallPosts = convertRSToClanWallPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanWallPosts;
  }
  
  private static List<ClanWallPost> convertRSToClanWallPosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<ClanWallPost> wallPosts = new ArrayList<ClanWallPost>();
        while(rs.next()) {
          ClanWallPost pwp = convertRSRowToClanWallPost(rs);
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

  private static ClanWallPost convertRSToSingleClanWallPost(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          ClanWallPost pwp = convertRSRowToClanWallPost(rs);
          return pwp;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static ClanWallPost convertRSRowToClanWallPost(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int clanId = rs.getInt(i++);
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    String content = rs.getString(i++);

    ClanWallPost pwp = new ClanWallPost(id, posterId, clanId, timeOfPost, content);
  
    return pwp;
  }
}
