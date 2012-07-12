package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostToMarketplaceRequestEvent;
import com.lvl6.events.response.PostToMarketplaceResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PostToMarketplaceRequestProto;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto.Builder;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto.PostToMarketplaceStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PostToMarketplaceController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());


  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  
  public PostToMarketplaceController() {
    numAllocatedThreads = 3;
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

    int diamondCost = reqProto.getDiamondCost();
    int coinCost = reqProto.getCoinCost();
    Timestamp timeOfPost = new Timestamp(new Date().getTime());

    PostToMarketplaceResponseProto.Builder resBuilder = PostToMarketplaceResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      List<UserEquip> userEquipsForEquipId = RetrieveUtils.userEquipRetrieveUtils().getUserEquipsWithEquipId(user.getId(), reqProto.getPostedEquipId());
      UserEquip ue = MiscMethods.chooseUserEquipWithEquipIdPreferrablyNonEquipped(user, userEquipsForEquipId);
      
      Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      Equipment equip = (ue == null) ? null : equipmentIdsToEquipment.get(ue.getEquipId());
      boolean legitPost = checkLegitPost(user, resBuilder, reqProto, diamondCost, coinCost, ue, equip, timeOfPost);

      PostToMarketplaceResponseEvent resEvent = new PostToMarketplaceResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPostToMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPost) {
        MarketplacePostType postType;
        if (diamondCost > 0) {
          postType = MarketplacePostType.PREMIUM_EQUIP_POST;
        } else {
          postType = MarketplacePostType.NORM_EQUIP_POST;
        }
        writeChangesToDB(user, reqProto, ue, userEquipsForEquipId, postType, timeOfPost);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.POST_TO_MARKETPLACE, true);
      }

    } catch (Exception e) {
      log.error("exception in PostToMarketplaceController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegitPost(User user, Builder resBuilder, 
      PostToMarketplaceRequestProto reqProto, int diamondCost, int coinCost, UserEquip userEquip, Equipment equip, Timestamp timeOfPost) {
    if (user == null || equip == null || timeOfPost == null) {
      resBuilder.setStatus(PostToMarketplaceStatus.OTHER_FAIL);
      log.error("a passed in parameter was null. user=" + user + ", equip=" + equip + ", timeOfPost=" + timeOfPost);
      return false;
    }
    if (userEquip == null) {
      resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_EQUIP);
      log.error("user does not have enough of equip with id " + equip.getId() + ", userEquip=" + userEquip);
      return false;
    }    
    if (user.getNumPostsInMarketplace() == ControllerConstants.POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER) {
      resBuilder.setStatus(PostToMarketplaceStatus.USER_ALREADY_MAX_MARKETPLACE_POSTS);
      log.error("user already has max num of marketplace posts, which is " + ControllerConstants.POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER);
      return false;      
    }
    if (diamondCost < 0 || coinCost < 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NEGATIVE_COST);
      log.error("item listed for a negative cost, diamondCost=" + diamondCost + ", coinCost=" + coinCost);
      return false;
    }
    if (diamondCost > 0 && coinCost > 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.CANT_DEMAND_BOTH);
      log.error("item asks for both diamonds and coins, diamondCost=" + diamondCost + ", coinCost=" + coinCost);
      return false;
    }
    if (diamondCost == 0 && coinCost == 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NO_COST);
      log.error("item listed for no currency");
      return false;
    }
    if (diamondCost > 0) {
      if (equip.getDiamondPrice() <= 0 && equip.getRarity() != Rarity.EPIC && equip.getRarity() != Rarity.LEGENDARY) {
        resBuilder.setStatus(PostToMarketplaceStatus.INVALID_COST_TYPE_FOR_POST);
        log.error("can't list diamond price for this equip: " + equip + ". must have a diamond price or be legendary/epic");
        return false;        
      }
    }
    if (coinCost > 0) {
      if (equip.getCoinPrice() <= 0 || equip.getRarity() == Rarity.EPIC || equip.getRarity() == Rarity.LEGENDARY) {
        resBuilder.setStatus(PostToMarketplaceStatus.INVALID_COST_TYPE_FOR_POST);
        log.error("can't list coin price for this equip: " + equip + ". must have a coin price and cannot be rare or legenday");
        return false;        
      }
    }
    resBuilder.setStatus(PostToMarketplaceStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, PostToMarketplaceRequestProto reqProto, 
      UserEquip ue, List<UserEquip> userEquipsForEquipId, MarketplacePostType postType, Timestamp timeOfPost) {

    if (ue != null) {
      if (userEquipsForEquipId.size() <= 1) {
        if (!MiscMethods.unequipUserEquipIfEquipped(user, ue)) {
          log.error("problem with unequipping userequip" + ue.getId());
          return;
        }
      }
      if (!DeleteUtils.get().deleteUserEquip(ue.getId())) {
        log.error("problem with decrementing user equip with ue id: " + ue);
        return;
      }
    }

    if (!user.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(0, 0, 1)) {
      log.error("problem with increasing user's num marketplace posts by 1");
      return;
    }
    
    int posterId = reqProto.getSender().getUserId();
    int postedEquipId = reqProto.getPostedEquipId();
    int diamondCost = reqProto.getDiamondCost();
    int coinCost = reqProto.getCoinCost();
    if (!insertUtils.insertMarketplaceItem(posterId, postType, postedEquipId, 
        diamondCost, coinCost, timeOfPost)) {
      log.error("problem with inserting post into marketplace. posterId=" + posterId
          + ", postType=" + postType + ", postedEquipId=" + postedEquipId
          + ", diamondCost=" + diamondCost + ", coinCost=" + coinCost
          + ", timeOfPost=" + timeOfPost);      
    }
  } 

}