package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.MarketplacePost;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.utils.DBConnection;

public class MarketplacePostRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_MARKETPLACE;

  public static MarketplacePost getSpecificActiveMarketplacePost(int marketplacePostId) {
    log.info("retrieving specific marketplace posts");
    return convertRSToSingleMarketplacePost(DBConnection.selectRowsById(marketplacePostId, TABLE_NAME));
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsBeforePostId(int limit, int postId) {
    log.info("retrieving limited marketplace posts before certain id");
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.MARKETPLACE__ID, postId);
    return convertRSToMarketplacePosts(DBConnection.selectRowsAbsoluteAndOrderByDescLimitLessthan(null, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit, lessThanParamsToVals));
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsBeforePostIdForPoster(int limit, int postId, int posterId) {
    log.info("retrieving limited marketplace posts before certain id");
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.MARKETPLACE__ID, postId);
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    return convertRSToMarketplacePosts(DBConnection.selectRowsAbsoluteAndOrderByDescLimitLessthan(absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit, lessThanParamsToVals));
  }

  public static List<MarketplacePost> getMostRecentActiveMarketplacePosts(int limit) {
    log.info("retrieving limited marketplace posts");
    return convertRSToMarketplacePosts(DBConnection.selectRowsAbsoluteAndOrderByDescLimit(null, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit));
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsForPoster(int limit, int posterId) {
    log.info("retrieving limited marketplace posts");
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    return convertRSToMarketplacePosts(DBConnection.selectRowsAbsoluteAndOrderByDescLimit(absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit));
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
    if (postedEquipId == 0) postedEquipId = MarketplacePost.NOT_SET;

    int postedNumWood = rs.getInt(i++);
    if (postedNumWood == 0) postedNumWood = MarketplacePost.NOT_SET;

    int postedDiamonds = rs.getInt(i++);
    if (postedDiamonds == 0) postedDiamonds = MarketplacePost.NOT_SET;

    int postedCoins = rs.getInt(i++);
    if (postedCoins == 0) postedCoins = MarketplacePost.NOT_SET;
    
    int diamondCost = rs.getInt(i++);
    if (diamondCost == 0) diamondCost = MarketplacePost.NOT_SET;
    
    int coinCost = rs.getInt(i++);
    if (coinCost == 0) coinCost = MarketplacePost.NOT_SET;

    int woodCost = rs.getInt(i++);
    if (woodCost == 0) woodCost = MarketplacePost.NOT_SET;

    MarketplacePost mp = new MarketplacePost(id, posterId, postType, timeOfPost, 
        postedEquipId, postedNumWood, postedDiamonds, postedCoins, 
        diamondCost, coinCost, woodCost);
  
    return mp;
  }
}
