package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.ClanChatPost;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanChatPostRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_WALL_POSTS;

  public static ClanChatPost getSpecificActiveClanChatPost(int wallPostId) {
    log.debug("retrieving wall post with id " + wallPostId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, wallPostId, TABLE_NAME);
    ClanChatPost clanChatPost = convertRSToSingleClanChatPost(rs);
    DBConnection.get().close(rs, null, conn);
    return clanChatPost;
  }

  public static List<ClanChatPost> getMostRecentActiveClanChatPostsForClanBeforePostId(int limit, int postId, int clanId) {
    log.debug("retrieving " + limit + " player wall posts before certain postId " + postId + " for clan " + clanId);
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.CLAN_WALL_POSTS__ID, postId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_WALL_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, absoluteParams, TABLE_NAME, DBConstants.CLAN_WALL_POSTS__ID, limit, lessThanParamsToVals);
    List<ClanChatPost> clanChatPosts = convertRSToClanChatPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanChatPosts;
  }
  
  public static List<ClanChatPost> getMostRecentClanChatPostsForClan(int limit, int clanId) {
    log.debug("retrieving " + limit + " clan wall posts for clan " + clanId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_WALL_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParams, TABLE_NAME, DBConstants.CLAN_WALL_POSTS__ID, limit);
    List<ClanChatPost> clanChatPosts = convertRSToClanChatPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanChatPosts;
  }
  
  private static List<ClanChatPost> convertRSToClanChatPosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<ClanChatPost> wallPosts = new ArrayList<ClanChatPost>();
        while(rs.next()) {
          ClanChatPost pwp = convertRSRowToClanChatPost(rs);
          if (pwp != null) wallPosts.add(pwp);
        }
        return wallPosts;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static ClanChatPost convertRSToSingleClanChatPost(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          ClanChatPost pwp = convertRSRowToClanChatPost(rs);
          return pwp;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static ClanChatPost convertRSRowToClanChatPost(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int clanId = rs.getInt(i++);
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    String content = rs.getString(i++);

    ClanChatPost pwp = new ClanChatPost(id, posterId, clanId, timeOfPost, content);
  
    return pwp;
  }
}
