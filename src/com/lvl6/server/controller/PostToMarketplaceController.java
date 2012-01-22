package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostToMarketplaceRequestEvent;
import com.lvl6.events.response.PostToMarketplaceResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.proto.EventProto.PostToMarketplaceRequestProto;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto.Builder;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto.PostToMarketplaceStatus;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class PostToMarketplaceController extends EventController {

  private static final double PERCENT_CUT_OF_FINAL_PRICE_TAKEN = .15;

  @Override  
  public void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PostToMarketplaceRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_POST_TO_MARKETPLACE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    PostToMarketplaceRequestProto reqProto = ((PostToMarketplaceRequestEvent)event).getPostToMarketplaceRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    MarketplacePostType postType = reqProto.getPostType();

    PostToMarketplaceResponseProto.Builder resBuilder = PostToMarketplaceResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      int diamondCost = reqProto.getDiamondCost();
      int coinCost = reqProto.getCoinCost();
      int woodCost = reqProto.getWoodCost();

      int diamondCut = (int)(Math.ceil(diamondCost * PERCENT_CUT_OF_FINAL_PRICE_TAKEN));
      int coinCut = (int)(Math.ceil(coinCost * PERCENT_CUT_OF_FINAL_PRICE_TAKEN));
      int woodCut = (int)(Math.ceil(woodCost * PERCENT_CUT_OF_FINAL_PRICE_TAKEN));

      UserEquip ue = null;
      if (postType == MarketplacePostType.EQUIP_POST) {
        ue = UserEquipRetrieveUtils.getSpecificUserEquip(user.getId(), reqProto.getPostedEquipId());
      }

      boolean legitPost = checkLegitPost(postType, user, resBuilder, reqProto, diamondCost, 
          coinCost, woodCost, diamondCut, coinCut, woodCut, ue);

      PostToMarketplaceResponseEvent resEvent = new PostToMarketplaceResponseEvent(senderProto.getUserId());
      resEvent.setPostToMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPost) {
        int postedDiamonds = Math.max(reqProto.getPostedDiamonds(), 0);
        int postedCoins = Math.max(reqProto.getPostedCoins(), 0);
        int postedWood = Math.max(reqProto.getPostedWood(), 0);
        writeChangesToDB(user, reqProto, postedDiamonds+diamondCut, postedCoins+coinCut, postedWood+woodCut, ue);
      }

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in PostToMarketplaceController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegitPost(MarketplacePostType postType, User user, Builder resBuilder, 
      PostToMarketplaceRequestProto reqProto, int diamondCost, int coinCost, int woodCost, 
      int diamondCut, int coinCut, int woodCut, UserEquip userEquip) {

    if (diamondCost < 0 || coinCost < 0 || woodCost < 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NEGATIVE_COST);
      return false;
    }
    if (diamondCost == 0 && coinCost == 0 && woodCost == 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NO_COST);
      return false;
    }
    if (user.getDiamonds() < diamondCut) {
      resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    if (user.getCoins() < coinCut) {
      resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_COINS);
      return false;
    }
    if (user.getWood() < woodCut) {
      resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_WOOD);
      return false;
    }
    if (reqProto.getPostedCoins() < 0 || reqProto.getPostedDiamonds() < 0 || 
        reqProto.getPostedWood() < 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NEGATIVE_POST);
      return false;
    }
    if (postType == MarketplacePostType.COIN_POST) {
      if (user.getCoins() < reqProto.getPostedCoins() + coinCut) {
        resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_COINS);
        return false;
      }
    } else if (postType == MarketplacePostType.DIAMOND_POST) {
      if (user.getDiamonds() < reqProto.getPostedDiamonds() + diamondCut) {
        resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_DIAMONDS);
        return false;
      }      
    } else if (postType == MarketplacePostType.WOOD_POST) {
      if (user.getWood() < reqProto.getPostedWood() + woodCut) {
        resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_WOOD);
        return false;
      }      
    } else if (postType == MarketplacePostType.EQUIP_POST) {
      if (userEquip == null || userEquip.getQuantity() < 1) {
        resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_EQUIP);
        return false;
      }
    } else {
      resBuilder.setStatus(PostToMarketplaceStatus.OTHER_FAIL);
      return false;
    }

    resBuilder.setStatus(PostToMarketplaceStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, PostToMarketplaceRequestProto reqProto, 
      int diamondChange, int coinChange, int woodChange, UserEquip ue) {

    if (diamondChange > 0 || coinChange > 0 || woodChange > 0) {
      if (!user.updateRelativeDiamondsCoinsWoodNaive(diamondChange*-1, coinChange*-1, woodChange*-1)) {
        log.error("problem with updating user stats post-marketplace-post");
      }
    }
    if (ue != null) {
      if (!UpdateUtils.decrementUserEquip(user.getId(), reqProto.getPostedEquipId(), 
          ue.getQuantity(), 1)) {
        log.error("problem with updating user equips post-marketplace-post");
      }
    }

    int posterId = reqProto.getSender().getUserId();
    MarketplacePostType postType = reqProto.getPostType();
    int postedEquipId = reqProto.getPostedEquipId();
    int postedWood = reqProto.getPostedWood();
    int postedDiamonds = reqProto.getPostedDiamonds();
    int postedCoins = reqProto.getPostedCoins();
    int diamondCost = reqProto.getDiamondCost();
    int coinCost = reqProto.getCoinCost();
    int woodCost = reqProto.getWoodCost();
    if (!InsertUtils.insertMarketplaceItem(posterId, postType, postedEquipId, 
        postedWood, postedDiamonds, postedCoins, diamondCost, 
        coinCost, woodCost)) {
      log.error("problem with inserting into marketplace");      
    }
  } 

}