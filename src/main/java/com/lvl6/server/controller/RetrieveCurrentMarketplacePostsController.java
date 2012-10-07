package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrentMarketplacePostsRequestEvent;
import com.lvl6.events.response.RetrieveCurrentMarketplacePostsResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto.RetrieveCurrentMarketplacePostsFilter;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto.RetrieveCurrentMarketplacePostsSortingOrder;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto.RetrieveCurrentMarketplacePostsStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

  @Component @DependsOn("gameServer") public class RetrieveCurrentMarketplacePostsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  @Autowired
  protected LeaderBoardUtil leaderboard;

  public LeaderBoardUtil getLeaderboard() {
	return leaderboard;
	}
	
	public void setLeaderboard(LeaderBoardUtil leaderboard) {
		this.leaderboard = leaderboard;
	}
  
  public RetrieveCurrentMarketplacePostsController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveCurrentMarketplacePostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
  }

  /* Returns the kingdom.equipment type associated with what the user selected in the filter
   * The value is -1 if the user wants all equipment types.
   */
  private int getEquipType(RetrieveCurrentMarketplacePostsFilter marketEquipType) {
	int eqType =  -1;
	int marketEquipEnum = marketEquipType.getNumber();
	switch(marketEquipEnum){
		case(RetrieveCurrentMarketplacePostsFilter.WEAPONS_VALUE):
			eqType = EquipType.WEAPON_VALUE;
			break;
		case(RetrieveCurrentMarketplacePostsFilter.ARMOR_VALUE):
			eqType = EquipType.ARMOR_VALUE;
			break;
		case(RetrieveCurrentMarketplacePostsFilter.AMULETS_VALUE):
			eqType = EquipType.AMULET_VALUE;
			break;
		default:
			break;
	}
	return eqType;
  }
  
  /* Returns only the values of the rarities the user selected
   * 
   */
  private List<Integer> getActiveEquipmentRarities(boolean commonEquips, boolean uncommonEquips,
		  boolean rareEquips, boolean epicEquips, boolean legendaryEquips) {
	List<Integer> equipRarities = new ArrayList<Integer>();
  	if(commonEquips){ equipRarities.add(Rarity.COMMON_VALUE); }
  	if(uncommonEquips){ equipRarities.add(Rarity.UNCOMMON_VALUE); }
  	if(rareEquips){ equipRarities.add(Rarity.RARE_VALUE); }
  	if(epicEquips){ equipRarities.add(Rarity.EPIC_VALUE); }
  	if(legendaryEquips){ equipRarities.add(Rarity.LEGENDARY_VALUE); }
  	
  	return equipRarities;
  }
  
  /* Put values into a map so less variables
   *
   */
  private Map<String, Integer> getLevelRanges(int minEquipLevel, int maxEquipLevel, int minForgeLevel, int maxForgeLevel){
	Map<String, Integer> levelRanges = new HashMap<String, Integer>();
  	levelRanges.put("minEquipLevel", minEquipLevel);
  	levelRanges.put("maxEquipLevel", maxEquipLevel);
  	levelRanges.put("minForgeLevel", minForgeLevel);
  	levelRanges.put("maxForgeLevel", maxForgeLevel);
  	return levelRanges;
  }
  
  /* Return class type based on what user selected in filter.
   * The value is -1 if the user wants all class types.
   */
  int getClassType(UserType theUsersType, boolean filterByUsersType) { 
	  if(filterByUsersType) { return theUsersType.getNumber(); }
	  else { return -1; }
  }
  
  /* Ordering by name always at the end.
   * TODO:Order by marketplace.id (this is ordering by when people placed item on the market)
   */
  String getSortOrder (RetrieveCurrentMarketplacePostsSortingOrder sortOrder) {
	  String sortingOrder = null;
	  String exponent = " (" + DBConstants.MARKETPLACE__EQUIP_LEVEL + " - 1)";
	  String base = Double.toString(ControllerConstants.LEVEL_EQUIP_BOOST_EXPONENT_BASE); //broke the abstraction :(
	  String exponentValue = " power( " + base + "," + exponent + ")"; 
	  
	  String atk = DBConstants.EQUIPMENT__ATK_BOOST;
	  String def = DBConstants.EQUIPMENT__DEF_BOOST;
	  
	  String marketplaceId = DBConstants.TABLE_MARKETPLACE + "." + DBConstants.MARKETPLACE__ID;
	  
	  switch (sortOrder) {
	  	case PRICE_HIGH_TO_LOW:
	  		sortingOrder = DBConstants.MARKETPLACE__DIAMOND_COST + " DESC";
	  		sortingOrder += ", " + DBConstants.MARKETPLACE__COIN_COST + " DESC";
	  		break;
	  	case PRICE_LOW_TO_HIGH:
	  		sortingOrder = DBConstants.MARKETPLACE__DIAMOND_COST + " ASC";
	  		sortingOrder += ", " + DBConstants.MARKETPLACE__COIN_COST + " ASC";
	  		break;
	  	case ATTACK_HIGH_TO_LOW:
	  		sortingOrder = atk + " * " + exponentValue + " DESC";
	  		break;
	  	case DEFENSE_HIGH_TO_LOW:
	  		sortingOrder = def + " * " + exponentValue + " DESC";
	  		break;
	  	case TOTAL_STATS_HIGH_TO_LOW:
	  		sortingOrder = "( " + atk + " + " + def + " ) * " + exponentValue + " DESC";
	  		break;
	  	case MOST_RECENT_POSTS:
	  		sortingOrder = marketplaceId  + " DESC";
	  		break;
	  	default:
	  		break;
	  }
	  sortingOrder += ", " + DBConstants.EQUIPMENT__NAME + " ASC";
	  return sortingOrder;
  }
  
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveCurrentMarketplacePostsRequestProto reqProto = ((RetrieveCurrentMarketplacePostsRequestEvent)event).getRetrieveCurrentMarketplacePostsRequestProto();

    //begin market filter feature variable declarations
    MinimumUserProto senderProto = reqProto.getSender();
    int currentNumOfEntries = reqProto.getCurrentNumOfEntries(); //number of items from db user has seen
    boolean forSender = reqProto.getFromSender(); //filter items posted to marketplace by sender(true), or everyone(false)
    RetrieveCurrentMarketplacePostsFilter marketEquipType = reqProto.getFilter();
    boolean commonEquips = reqProto.getCommonEquips();
    boolean uncommonEquips = reqProto.getUncommonEquips();
    boolean rareEquips = reqProto.getRareEquips();
    boolean epicEquips = reqProto.getEpicEquips();
    boolean legendaryEquips = reqProto.getLegendaryEquips();
    boolean myClassOnly = reqProto.getMyClassOnly();
    int minEquipLevel = reqProto.getMinEquipLevel();
    int maxEquipLevel = reqProto.getMaxEquipLevel();
    int minForgeLevel = reqProto.getMinForgeLevel();
    int maxForgeLevel = reqProto.getMaxForgeLevel();
    int searchEquipId = reqProto.getSpecificEquipId();
    RetrieveCurrentMarketplacePostsSortingOrder sortOrder = reqProto.getSortOrder();
    //end market filter feature variable declarations

    RetrieveCurrentMarketplacePostsResponseProto.Builder resBuilder = RetrieveCurrentMarketplacePostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setFromSender(forSender);
    if (currentNumOfEntries > 0) {
      resBuilder.setBeforeThisPostId(currentNumOfEntries);
    }

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      if (user == null) {
        resBuilder.setStatus(RetrieveCurrentMarketplacePostsStatus.OTHER_FAIL);
        log.error("no user with given id");
      } else {
        resBuilder.setStatus(RetrieveCurrentMarketplacePostsStatus.SUCCESS);
        List <MarketplacePost> activeMarketplacePosts;
        //boolean populateMarketplace = false;
    	if (forSender) {
            if(currentNumOfEntries > 0) {
            	activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostIdForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, currentNumOfEntries, senderProto.getUserId());
            } else{
            	activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, senderProto.getUserId());
            }
    	} else {
			int equipmentType = getEquipType(marketEquipType);
    	
			List<Integer> activeEquipRarities = getActiveEquipmentRarities(commonEquips, uncommonEquips, rareEquips, epicEquips, legendaryEquips);

			int characterClassType = getClassType(senderProto.getUserType(), myClassOnly);
    	
			Map<String, Integer> levelRanges = getLevelRanges(minEquipLevel, maxEquipLevel, minForgeLevel, maxForgeLevel);

			String orderBySql = getSortOrder(sortOrder);
    			
			activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsByFilters(
        		ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, currentNumOfEntries, 
        		equipmentType, activeEquipRarities, characterClassType, levelRanges, orderBySql, searchEquipId);        
    	}
//        if (currentNumOfEntries > 0) {
//          if (forSender) {
//            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostIdForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, currentNumOfEntries, senderProto.getUserId());
//          } else {
//        	int equipmentType = getEquipType(marketEquipType);
//        	
//        	List<Integer> activeEquipRarities = getActiveEquipmentRarities(commonEquips, uncommonEquips, rareEquips, epicEquips, legendaryEquips);
//
//        	int characterClassType = getClassType(senderProto.getUserType(), myClassOnly);
//        	
//        	Map<String, Integer> levelRanges = getLevelRanges(minEquipLevel, maxEquipLevel, minForgeLevel, maxForgeLevel);
//
//        	String orderBySql = getSortOrder(sortOrder);
//        			
//            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsByFilters(
//            		ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, currentNumOfEntries, 
//            		equipmentType, activeEquipRarities, characterClassType, levelRanges, orderBySql, searchString);        
//          }
//        } else {
//          if (forSender) {
//            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, senderProto.getUserId());
//          } else {
//            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP);
//            populateMarketplace = checkWhetherToPopulateMarketplace(activeMarketplacePosts, user);
//          }
//        }
//        int i = 0;
//        while (populateMarketplace && i < ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__MAX_NUM_POPULATE_RETRIES) {
//          populateMarketplaceWithPosts(user);
//          activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP);
//          populateMarketplace = checkWhetherToPopulateMarketplace(activeMarketplacePosts, user);
//          i++;
//        }
        if (activeMarketplacePosts != null && activeMarketplacePosts.size() > 0) {
          List <Integer> userIds = new ArrayList<Integer>();
          
          for (MarketplacePost amp : activeMarketplacePosts) {
            userIds.add(amp.getPosterId());
          }

          Map<Integer, User> usersByIds = null;
          if (userIds.size() > 0) {
            usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
          }
          for (MarketplacePost mp : activeMarketplacePosts) {
            resBuilder.addMarketplacePosts(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, usersByIds.get(mp.getPosterId())));
          }
        } 
      }
      RetrieveCurrentMarketplacePostsResponseProto resProto = resBuilder.build();

      RetrieveCurrentMarketplacePostsResponseEvent resEvent = new RetrieveCurrentMarketplacePostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrentMarketplacePostsResponseProto(resProto);

      server.writeEvent(resEvent);
      //leaderboard.updateLeaderboardCoinsForUser(user.getId());
    } catch (Exception e) {
      log.error("exception in RetrieveCurrentMarketplacePostsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }

  }

//  private boolean checkWhetherToPopulateMarketplace(List<MarketplacePost> activeMarketplacePosts, User user) {
//    if (activeMarketplacePosts == null || activeMarketplacePosts.size() <= 0) {
//      return true;
//    }
//    if (activeMarketplacePosts.size() >= ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__MIN_NUM_OF_POSTS_FOR_NO_POPULATE) {
//      return false;
//    }
//    Equipment equipToPost = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE);
//    for (MarketplacePost mp : activeMarketplacePosts) {
//      if (mp.getPostedEquipId() == ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE
//    		  && (mp.getPosterId() != user.getId()) && mp.getCoinCost() < equipToPost.getCoinPrice()) {
//        return false;
//      }
//    }
//    return true;
//  }

//  private void populateMarketplaceWithPosts(User user) {
//	  int equipIdToPopulate = ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE;
//	  int[] fakePosterIds = ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_POSTER_IDS;
//	  int fakePosterId = fakePosterIds[(int) (Math.random()*fakePosterIds.length)];
//	  MarketplacePostType postType;
//	  int diamondCost = 0;
//	  int coinCost = 0;
//	  Timestamp timeOfPost = new Timestamp(new Date().getTime());
//	  Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipIdToPopulate);
//	  
//	  if (!equip.isBuyableInArmory()) {
//		  log.error("ERROR, equip " + equip + "is not buyable in armory!");
//		  return;
//	  }
//
//	  if (equip.getDiamondPrice() <= 0 && equip.getRarity() != Rarity.EPIC && equip.getRarity() != Rarity.LEGENDARY) {
//		  postType = MarketplacePostType.NORM_EQUIP_POST;		
//		  coinCost = (int) (equip.getCoinPrice()*ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING);
//	  } else {
//		  postType = MarketplacePostType.PREMIUM_EQUIP_POST;
//		  diamondCost = (int) (equip.getDiamondPrice()*ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING);
//	  }
//      
//	  if (!InsertUtils.get().insertMarketplaceItem(fakePosterId, postType, equipIdToPopulate, diamondCost, coinCost, timeOfPost, 
//	      ControllerConstants.DEFAULT_USER_EQUIP_LEVEL)) {
//      log.error("problem with inserting fake post into marketplace. posterId=" + fakePosterId
//          + ", postType=" + postType + ", postedEquipId=" + equipIdToPopulate
//          + ", diamondCost=" + diamondCost + ", coinCost=" + coinCost
//          + ", timeOfPost=" + timeOfPost + ", equipLevel = " + ControllerConstants.DEFAULT_USER_EQUIP_LEVEL);      
//	  }
//  }

}
