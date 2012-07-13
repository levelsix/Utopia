package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrentMarketplacePostsRequestEvent;
import com.lvl6.events.response.RetrieveCurrentMarketplacePostsResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto.RetrieveCurrentMarketplacePostsStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;

  @Component @DependsOn("gameServer") public class RetrieveCurrentMarketplacePostsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveCurrentMarketplacePostsRequestProto reqProto = ((RetrieveCurrentMarketplacePostsRequestEvent)event).getRetrieveCurrentMarketplacePostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int beforeThisPostId = reqProto.getBeforeThisPostId();
    boolean forSender = reqProto.getFromSender();

    RetrieveCurrentMarketplacePostsResponseProto.Builder resBuilder = RetrieveCurrentMarketplacePostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setFromSender(forSender);
    if (beforeThisPostId > 0) {
      resBuilder.setBeforeThisPostId(beforeThisPostId);
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
        boolean populateMarketplace = false;
        if (beforeThisPostId > 0) {
          if (forSender) {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostIdForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, beforeThisPostId, senderProto.getUserId());
          } else {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostId(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, beforeThisPostId);        
          }
        } else {
          if (forSender) {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, senderProto.getUserId());
          } else {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP);
            populateMarketplace = checkWhetherToPopulateMarketplace(activeMarketplacePosts);
          }
        }
        int i = 0;
        while (populateMarketplace && i < ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__MAX_NUM_POPULATE_RETRIES) {
          populateMarketplaceWithPosts(user);
          activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP);
          populateMarketplace = checkWhetherToPopulateMarketplace(activeMarketplacePosts);
          i++;
        }
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
    } catch (Exception e) {
      log.error("exception in RetrieveCurrentMarketplacePostsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }

  }

  private boolean checkWhetherToPopulateMarketplace(List<MarketplacePost> activeMarketplacePosts) {
    if (activeMarketplacePosts == null || activeMarketplacePosts.size() <= 0) {
      return true;
    }
    if (activeMarketplacePosts.size() >= ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__MIN_NUM_OF_POSTS_FOR_NO_POPULATE) {
      return false;
    }
    for (MarketplacePost mp : activeMarketplacePosts) {
      if (mp.getPostedEquipId() == ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE) {
        return false;
      }
    }
    return true;
  }

  private void populateMarketplaceWithPosts(User user) {
	  int equipIdToPopulate = ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE;
	  int[] fakePosterIds = ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_POSTER_IDS;
	  int fakePosterId = fakePosterIds[(int) (Math.random()*fakePosterIds.length)];
	  MarketplacePostType postType;
	  int diamondCost = 0;
	  int coinCost = 0;
	  Timestamp timeOfPost = new Timestamp(new Date().getTime());
	  Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipIdToPopulate);
	  
	  if (!equip.isBuyableInArmory()) {
		  log.error("ERROR, equip " + equip + "is not buyable in armory!");
		  return;
	  }

	  if (equip.getDiamondPrice() <= 0 && equip.getRarity() != Rarity.EPIC && equip.getRarity() != Rarity.LEGENDARY) {
		  postType = MarketplacePostType.NORM_EQUIP_POST;		
		  coinCost = (int) (equip.getCoinPrice()*ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING);
	  } else {
		  postType = MarketplacePostType.PREMIUM_EQUIP_POST;
		  diamondCost = (int) (equip.getDiamondPrice()*ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING);
	  }
      
	  InsertUtils.get().insertMarketplaceItem(fakePosterId, postType, equipIdToPopulate, diamondCost, coinCost, timeOfPost);
  }

}
