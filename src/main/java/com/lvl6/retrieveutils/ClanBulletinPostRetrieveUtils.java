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

import com.lvl6.info.ClanBulletinPost;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanBulletinPostRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_BULLETIN_POSTS;

  public static ClanBulletinPost getSpecificActiveClanBulletinPost(int bulletinPostId) {
    log.debug("retrieving bulletin post with id " + bulletinPostId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, bulletinPostId, TABLE_NAME);
    ClanBulletinPost clanBulletinPost = convertRSToSingleClanBulletinPost(rs);
    DBConnection.get().close(rs, null, conn);
    return clanBulletinPost;
  }

  public static List<ClanBulletinPost> getMostRecentActiveClanBulletinPostsForClanBeforePostId(int limit, int postId, int clanId) {
    log.debug("retrieving " + limit + " player bulletin posts before certain postId " + postId + " for clan " + clanId);
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.CLAN_BULLETIN_POSTS__ID, postId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_BULLETIN_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, absoluteParams, TABLE_NAME, DBConstants.CLAN_BULLETIN_POSTS__ID, limit, lessThanParamsToVals);
    List<ClanBulletinPost> clanBulletinPosts = convertRSToClanBulletinPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanBulletinPosts;
  }
  
  public static List<ClanBulletinPost> getMostRecentClanBulletinPostsForClan(int limit, int clanId) {
    log.debug("retrieving " + limit + " clan bulletin posts for clan " + clanId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_BULLETIN_POSTS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParams, TABLE_NAME, DBConstants.CLAN_BULLETIN_POSTS__ID, limit);
    List<ClanBulletinPost> clanBulletinPosts = convertRSToClanBulletinPosts(rs);
    DBConnection.get().close(rs, null, conn);
    return clanBulletinPosts;
  }
  
  private static List<ClanBulletinPost> convertRSToClanBulletinPosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<ClanBulletinPost> bulletinPosts = new ArrayList<ClanBulletinPost>();
        while(rs.next()) {
          ClanBulletinPost pwp = convertRSRowToClanBulletinPost(rs);
          if (pwp != null) bulletinPosts.add(pwp);
        }
        return bulletinPosts;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static ClanBulletinPost convertRSToSingleClanBulletinPost(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          ClanBulletinPost pwp = convertRSRowToClanBulletinPost(rs);
          return pwp;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static ClanBulletinPost convertRSRowToClanBulletinPost(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int clanId = rs.getInt(i++);
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    String content = rs.getString(i++);

    ClanBulletinPost pwp = new ClanBulletinPost(id, posterId, clanId, timeOfPost, content);
  
    return pwp;
  }
}
