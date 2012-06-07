package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

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
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class RetractMarketplacePostController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetractMarketplacePostController() {
    numAllocatedThreads = 3;
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
  protected void processRequestEvent(RequestEvent event) throws Exception {
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

      int diamondCut = Math.max(0, (int)(Math.ceil(diamondCost * ControllerConstants.RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)));
      int coinCut = Math.max(0, (int)(Math.ceil(coinCost * ControllerConstants.RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)));
      
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitRetract = checkLegitRetract(user, mp, resBuilder, 
          diamondCut, coinCut, postId);

      RetractMarketplacePostResponseEvent resEvent = new RetractMarketplacePostResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetractMarketplacePostResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRetract) {
        writeChangesToDB(user, mp, diamondCut, coinCut);
        if (mp != null) {
          UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
          resEventUpdate.setTag(event.getTag());
          server.writeEvent(resEventUpdate);
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
      }
    } catch (Exception e) {
      log.error("exception in RetractMarketplacePostController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }

  }

  private void writeChangesToDB(User user, MarketplacePost mp, int diamondCut, int coinCut) {
    if (user == null || mp == null) {
      log.error("parameter passed in is null. user=" + user + ", marketplace post=" + mp);
    }

    int diamondChange = diamondCut * -1;
    int coinChange = coinCut * -1;
    
    if (!UpdateUtils.incrementUserEquip(user.getId(), mp.getPostedEquipId(), 1)) {
      log.error("problem with giving user 1 more of equip " + mp.getPostedEquipId());
    }
    if (!user.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(diamondChange, coinChange, -1)) {
      log.error("problem with decrementing user's num posts in marketplace by 1 and changing diamonds by "
          + diamondChange + " and changing coins by " + coinChange);
    }

    if (!DeleteUtils.deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post with id " + mp.getId());      
    }
  }

  private boolean checkLegitRetract(User user, MarketplacePost mp, Builder resBuilder, 
      int diamondCut, int coinCut, int postId) {
    if (mp == null) {
      resBuilder.setStatus(RetractMarketplacePostStatus.POST_NO_LONGER_EXISTS);
      log.warn("problem with retracting marketplace post with id " + postId + " b/c no longer exists");      
      return false;
    }
    if (user.getId() != mp.getPosterId()) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_REQUESTERS_POST);
      log.error("trying to retract a post that does not belong to the user. poster's id is " + mp.getPosterId());
      return false;      
    }
    if (user.getDiamonds() < diamondCut) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user doesn't have enough diamonds. has " + user.getDiamonds() + ", needs " + diamondCut);
      return false;
    }
    if (user.getCoins() < coinCut) {
      resBuilder.setStatus(RetractMarketplacePostStatus.NOT_ENOUGH_COINS);
      log.error("user doesn't have enough coins. has " + user.getCoins() + ", needs " + coinCut);
      return false;
    }
    resBuilder.setStatus(RetractMarketplacePostStatus.SUCCESS);
    return true;
  }

}
