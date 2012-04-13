package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.MarketplacePost;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.utils.DBConnection;

public class MarketplacePostRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_MARKETPLACE;

  public static MarketplacePost getSpecificActiveMarketplacePost(int marketplacePostId) {
    log.debug("retrieving specific marketplace posts");
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsById(conn, marketplacePostId, TABLE_NAME);
    MarketplacePost marketplacePost = convertRSToSingleMarketplacePost(rs);
    DBConnection.close(rs, null, conn);
    return marketplacePost;
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsBeforePostId(int limit, int postId) {
    log.debug("retrieving limited marketplace posts before certain id");
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.MARKETPLACE__ID, postId);
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, null, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit, lessThanParamsToVals);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.close(rs, null, conn);
    return marketplacePosts;
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsBeforePostIdForPoster(int limit, int postId, int posterId) {
    log.debug("retrieving limited marketplace posts before certain id");
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.MARKETPLACE__ID, postId);
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit, lessThanParamsToVals);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.close(rs, null, conn);
    return marketplacePosts;
  }

  public static List<MarketplacePost> getMostRecentActiveMarketplacePosts(int limit) {
    log.debug("retrieving limited marketplace posts");
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescLimit(conn, null, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.close(rs, null, conn);
    return marketplacePosts;
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsForPoster(int limit, int posterId) {
    log.debug("retrieving limited marketplace posts");
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.close(rs, null, conn);
    return marketplacePosts;
  }
  
  private static List<MarketplacePost> convertRSToMarketplacePosts(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<MarketplacePost> activeMarketplacePosts = new ArrayList<MarketplacePost>();
        while(rs.next()) {
          MarketplacePost marketplacePost = convertRSRowToMarketplacePost(rs);
          activeMarketplacePosts.add(marketplacePost);
        }
        return activeMarketplacePosts;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
  private static MarketplacePost convertRSToSingleMarketplacePost(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          MarketplacePost marketplacePost = convertRSRowToMarketplacePost(rs);
          return marketplacePost;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static MarketplacePost convertRSRowToMarketplacePost(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    MarketplacePostType postType = MarketplacePostType.valueOf(rs.getInt(i++));
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());

    int postedEquipId = rs.getInt(i++);
    int diamondCost = rs.getInt(i++);
    if (diamondCost == 0) diamondCost = ControllerConstants.NOT_SET;
    
    int coinCost = rs.getInt(i++);
    if (coinCost == 0) coinCost = ControllerConstants.NOT_SET;

    MarketplacePost mp = new MarketplacePost(id, posterId, postType, timeOfPost, 
        postedEquipId, diamondCost, coinCost);
  
    return mp;
  }
}
