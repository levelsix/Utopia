package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetractMarketplacePostRequestEvent;
import com.lvl6.events.response.RetractMarketplacePostResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetractMarketplacePostRequestProto;
import com.lvl6.proto.EventProto.RetractMarketplacePostResponseProto;
import com.lvl6.proto.EventProto.RetractMarketplacePostResponseProto.Builder;
import com.lvl6.proto.EventProto.RetractMarketplacePostResponseProto.RetractMarketplacePostStatus;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class RetractMarketplacePostController extends EventController{

  private static final double PERCENT_CUT_OF_SELLING_PRICE_TAKEN = ControllerConstants.RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN;
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RetractMarketplacePostRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRACT_POST_FROM_MARKETPLACE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetractMarketplacePostRequestProto reqProto = ((RetractMarketplacePostRequestEvent)event).getRetractMarketplacePostRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int postId = reqProto.getMarketplacePostId();

    RetractMarketplacePostResponseProto.Builder resBuilder = RetractMarketplacePostResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      MarketplacePost mp = MarketplacePostRetrieveUtils.getSpecificActiveMarketplacePost(postId);
      
      int diamondCost = mp.getDiamondCost();
      int coinCost = mp.getCoinCost();
      int woodCost = mp.getWoodCost();

      int diamondCut = (int)(Math.ceil(diamondCost * PERCENT_CUT_OF_SELLING_PRICE_TAKEN));
      int coinCut = (int)(Math.ceil(coinCost * PERCENT_CUT_OF_SELLING_PRICE_TAKEN));
      int woodCut = (int)(Math.ceil(woodCost * PERCENT_CUT_OF_SELLING_PRICE_TAKEN));
      
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitRetract = checkLegitRetract(user, mp, resBuilder, 
          diamondCut, coinCut, woodCut);

      RetractMarketplacePostResponseEvent resEvent = new RetractMarketplacePostResponseEvent(senderProto.getUserId());
      resEvent.setRetractMarketplacePostResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRetract) {
        writeChangesToDB(user, mp, diamondCut, coinCut, woodCut);

        if (mp != null && mp.getPostType() != MarketplacePostType.EQUIP_POST) {
          UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
          server.writeEvent(resEventUpdate);
        }
      }
    } catch (Exception e) {
      log.error("exception in RetractMarketplacePostController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }

  }

  private void writeChangesToDB(User user, MarketplacePost mp, int diamondCut, int coinCut, 
      int woodCut) {
    if (user == null || mp == null) {
      log.error("problem with retracting marketplace post");
    }

    MarketplacePostType postType = mp.getPostType();
    
    int diamondChange = diamondCut * -1;
    int coinChange = coinCut * -1;
    int woodChange = woodCut * -1;
    
    if (postType == MarketplacePostType.DIAMOND_POST) {
      diamondChange += mp.getPostedDiamonds();
    }
    if (postType == MarketplacePostType.COIN_POST) {
      coinChange += mp.getPostedCoins();
    }
    if (postType == MarketplacePostType.WOOD_POST) {
      woodChange += mp.getPostedWood();
    }

    if (postType == MarketplacePostType.EQUIP_POST) {
      if (!UpdateUtils.incrementUserEquip(user.getId(), mp.getPostedEquipId(), 1)) {
        log.error("problem with giving user back equip");
      }
    }
    if (!user.updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive(diamondChange, coinChange, woodChange, -1)) {
      log.error("problem with giving user back stuff after retract");
    }

    if (!DeleteUtils.deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post");      
    }
  }

  private boolean checkLegitRetract(User user, MarketplacePost mp, Builder resBuilder, 
      int diamondCut, int coinCut, int woodCut) {
    if (mp == null) {
      resBuilder.setStatus(RetractMarketplacePostStatus.POST_NO_LONGER_EXISTS);
      return false;
    }
    if (user.getId() != mp.getPosterId()) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_REQUESTERS_POST);
      return false;      
    }
    if (user.getDiamonds() < diamondCut) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    if (user.getCoins() < coinCut) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_ENOUGH_COINS);
      return false;
    }
    if (user.getWood() < woodCut) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_ENOUGH_WOOD);
      return false;
    }
    
    resBuilder.setStatus(RetractMarketplacePostStatus.SUCCESS);
    return true;
  }

}
