package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  public static List<MarketplacePost> getMostRecentActiveMarketplacePosts(int limit) {
    log.info("retrieving limited marketplace posts");
    return convertRSToMarketplacePosts(DBConnection.selectRowsAndOrderByDescLimit(null, TABLE_NAME, DBConstants.MARKETPLACE__TIME_OF_POST, limit));
  }
  
  public static List<MarketplacePost> getCurrentMarketplacePosts() {
    log.info("retrieving current marketplace posts");
    return convertRSToMarketplacePosts(DBConnection.selectRowsAndOrderByDesc(null, TABLE_NAME, DBConstants.MARKETPLACE__TIME_OF_POST));
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
        System.out.println("problem with database call.");
        e.printStackTrace();
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
        System.out.println("problem with database call.");
        e.printStackTrace();
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
