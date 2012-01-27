package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetractMarketplacePostRequestEvent;
import com.lvl6.events.response.RetractMarketplacePostResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
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

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());
  }

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

      boolean legitRetract = checkLegitRetract(senderProto.getUserId(), mp, resBuilder);

      RetractMarketplacePostResponseEvent resEvent = new RetractMarketplacePostResponseEvent(senderProto.getUserId());
      resEvent.setRetractMarketplacePostResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRetract) {
        User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
        writeChangesToDB(user, mp);

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

  private void writeChangesToDB(User user, MarketplacePost mp) {
    if (user == null || mp == null) {
      log.error("problem with retracting marketplace post");
    }

    MarketplacePostType postType = mp.getPostType();

    if (postType == MarketplacePostType.COIN_POST) {
      if (!user.updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive(0, mp.getPostedCoins(), 0, -1)) {
        log.error("problem with giving user back coins");
      }
    }
    if (postType == MarketplacePostType.DIAMOND_POST) {
      if (!user.updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive(mp.getPostedDiamonds(), 0, 0, -1)) {
        log.error("problem with giving user back diamonds");
      }
    }
    if (postType == MarketplacePostType.WOOD_POST) {
      if (!user.updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive(0, 0, mp.getPostedWood(), -1)) {
        log.error("problem with giving user back wood");
      }
    }
    if (postType == MarketplacePostType.EQUIP_POST) {
      if (!UpdateUtils.incrementUserEquip(user.getId(), mp.getPostedEquipId(), 1)) {
        log.error("problem with giving user back equip");
      }
      if (!user.updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive(0, 0, 0, -1)) {
        log.error("problem with bringing back marketplace num");
      }
    }

    if (!DeleteUtils.deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post");      
    }
  }

  private boolean checkLegitRetract(int userId, MarketplacePost mp, Builder resBuilder) {
    if (mp == null) {
      resBuilder.setStatus(RetractMarketplacePostStatus.POST_NO_LONGER_EXISTS);
      return false;
    }
    if (userId != mp.getPosterId()) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_REQUESTERS_POST);
      return false;      
    }
    resBuilder.setStatus(RetractMarketplacePostStatus.SUCCESS);
    return true;
  }

}
