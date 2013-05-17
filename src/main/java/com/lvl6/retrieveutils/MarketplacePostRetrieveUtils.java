package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.MarketplacePost;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.EquipClassType;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class MarketplacePostRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_MARKETPLACE;
  
  public static MarketplacePost getSpecificActiveMarketplacePost(int marketplacePostId) {
    log.debug("retrieving specific marketplace post with id " + marketplacePostId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, marketplacePostId, TABLE_NAME);
    MarketplacePost marketplacePost = convertRSToSingleMarketplacePost(rs);
    DBConnection.get().close(rs, null, conn);
    return marketplacePost;
  }
  
  //marketItemType refers to RetrieveCurrentMarketplacePostsRequestProto.RetrieveCurrentMarketplacePostsFilter enum
  //activeEquipRarities refers to the booleans like *Equips
  //levelRanges refers to *EquipLevel or *ForgeLevel
  //searchString currently not used
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsByFilters(int limit, int postId, int equipmentType, 
		  List<Integer> activeEquipRarities, int characterClassType, Map<String, Integer> levelRanges, 
		  String orderBySql, int specificEquipId, Timestamp timeOfRequest) {
    log.debug("retrieving up to " + limit + " marketplace posts before marketplace post id " + postId);

    //////////BEGIN SQL STATEMENT//////////
    //SELECT CLAUSE
    List<String> colsToFetch = new ArrayList<String>();
    colsToFetch.add(TABLE_NAME + ".*"); //get all columns
    
    //FROM CLAUSE
    String tableName = TABLE_NAME + ", " + DBConstants.TABLE_EQUIPMENT;
    
    //WHERE CLAUSE
    //begin absolute condition params
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    
    //matching 'ids of marketplace equipments' to ids in equipment table
    String hackyWayJoinTablesById = DBConstants.MARKETPLACE__POSTED_EQUIP_ID + " = " + DBConstants.TABLE_EQUIPMENT + "." + DBConstants.EQUIPMENT__EQUIP_ID;
    hackyWayJoinTablesById += " and 1"; 
    //the value for key value pair is stringified, so a table's field would be turned to a literal string and not be a variable/placeholder
    absoluteConditionParams.put(hackyWayJoinTablesById, 1);
    
    //Greater than -1 means filter by specific equipment type
    if(-1 < equipmentType) { absoluteConditionParams.put("type", equipmentType); }
    
    //Needed to prevent "rarity in ()" case
    int equipRaritiesCount = activeEquipRarities.size();
    if (0 < equipRaritiesCount){
    	String hackyWayToHaveAnotherCondition = DBConstants.EQUIPMENT__RARITY + " IN (";
    	
    	//needed to prevent "rarity in (0,)" case
    	String maybeComma = ",";
    	
    	//TODO: replace this with a call to StringUtils.java
    	for(int i = 0; i < equipRaritiesCount; i++){
	    	Integer rarityId = activeEquipRarities.get(i);
	    	if(i+1 == equipRaritiesCount){
	    		//this is to not have a trailing comma at the end of the string
	    		maybeComma = "";
	    	}
	    	hackyWayToHaveAnotherCondition += rarityId + maybeComma;
	    }
	    hackyWayToHaveAnotherCondition += ") and 1";
	    absoluteConditionParams.put(hackyWayToHaveAnotherCondition, 1);
    }
    
    if (specificEquipId > 0) {
      absoluteConditionParams.put(DBConstants.TABLE_EQUIPMENT + "." + DBConstants.EQUIPMENT__EQUIP_ID, specificEquipId);
    }
    
    //Greater than -1 means filter by specific class type
    if(-1 < characterClassType) {
      absoluteConditionParams.put("(class_type="+EquipClassType.ALL_AMULET_VALUE+" or class_type="+characterClassType+") and 1", 1); 
    }
    //end absolute condition params
    
    //Begin relative greater than condition params
    Map<String, Object> relativeGreaterThanConditionParams = new HashMap<String, Object>();
    //subtract 1 to be >=
    relativeGreaterThanConditionParams.put(DBConstants.EQUIPMENT__MIN_LEVEL, levelRanges.get("minEquipLevel") - 1); 
    relativeGreaterThanConditionParams.put(DBConstants.MARKETPLACE__EQUIP_LEVEL, levelRanges.get("minForgeLevel") - 1);
    //End relative greater than condition params
    
    //Begin relative less than condition params
    Map<String, Object> relativeLessThanConditionParams = new HashMap<String, Object>();
    //add one to be <=
    relativeLessThanConditionParams.put(DBConstants.EQUIPMENT__MIN_LEVEL, levelRanges.get("maxEquipLevel") + 1); 
    relativeLessThanConditionParams.put(DBConstants.MARKETPLACE__EQUIP_LEVEL, levelRanges.get("maxForgeLevel") + 1);
    
    //this will account for marketplace posts with a time_of_post value set in the future
    //meaning, retain marketplace posts that have been posted before the current time (timeOfRequest)
    relativeLessThanConditionParams.put(DBConstants.MARKETPLACE__TIME_OF_POST, timeOfRequest);
    //End relative less than condition params
    
    //ORDER BY CLAUSE
    String orderByColumn = orderBySql;
    boolean orderByAsc = true; //orderByColumn already contains the necessary ASC, DESC
    //////////END SQL STATEMENT//////////
    
    Map<String, Object> likeCondParams = null;
    String condDelim = "AND";
    boolean randomValue = false;
    int amountToRetrieve = postId + limit; //example (*): 110 + 100 = 210
    
    Connection conn = DBConnection.get().getConnection();    
    ResultSet rs = DBConnection.get().selectRows(conn, colsToFetch, absoluteConditionParams, 
    		relativeGreaterThanConditionParams, relativeLessThanConditionParams, likeCondParams, tableName,
    		condDelim, orderByColumn, orderByAsc, amountToRetrieve, randomValue);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    
    //PAGINATION OF ITEMS. SENDING NEXT BATCH OF NEW ITEMS
    //continuing example (*): indexes of returned items is [0,209], want to return [110, 209]
    //List<MarketplacePost> newMarketplacePosts = marketplacePosts.subList(postId, amountToRetrieve); 
    //previous can create indexOutOfBoundsException. We're already potentially getting an amount of new items equal to 'limit'
    //so using size of list as last index
    List<MarketplacePost> newMarketplacePosts = null;
    if (postId < marketplacePosts.size()) {
      newMarketplacePosts = marketplacePosts.subList(postId, marketplacePosts.size());
    }
    
    DBConnection.get().close(rs, null, conn);
    return newMarketplacePosts;
  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsBeforePostIdForPoster(int limit, int postId, int posterId) {
    log.debug("retrieving up to " + limit + " marketplace posts before marketplace post id " + postId + " for posterId " + posterId);
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.MARKETPLACE__ID, postId);
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit, lessThanParamsToVals);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.get().close(rs, null, conn);
    return marketplacePosts;
  }

//  public static List<MarketplacePost> getMostRecentActiveMarketplacePosts(int limit) {
//    log.debug("retrieving up to " + limit + " most recent marketplace posts");
//    
//    Connection conn = DBConnection.get().getConnection();
//    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, null, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit);
//    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
//    DBConnection.get().close(rs, null, conn);
//    return marketplacePosts;
//  }
  
  public static List<MarketplacePost> getMostRecentActiveMarketplacePostsForPoster(int limit, int posterId) {
    log.debug("retrieving up to " + limit + " marketplace posts for posterId " + posterId);
    TreeMap <String, Object> absoluteParamsToVals = new TreeMap<String, Object>();
    absoluteParamsToVals.put(DBConstants.MARKETPLACE__POSTER_ID, posterId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParamsToVals, TABLE_NAME, DBConstants.MARKETPLACE__ID, limit);
    List<MarketplacePost> marketplacePosts = convertRSToMarketplacePosts(rs);
    DBConnection.get().close(rs, null, conn);
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
        log.error("problem with database call.", e);
        
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
        log.error("problem with database call.", e);
        
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
    
    int equipLevel = rs.getInt(i++);
    int equipEnhacementPercent = rs.getInt(i++);
    
    MarketplacePost mp = new MarketplacePost(id, posterId, postType, timeOfPost, 
        postedEquipId, diamondCost, coinCost, equipLevel, equipEnhacementPercent);
  
    return mp;
  }
}
