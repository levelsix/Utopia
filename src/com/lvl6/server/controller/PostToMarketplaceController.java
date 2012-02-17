package com.lvl6.server.controller;

import java.util.Map;

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
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class PostToMarketplaceController extends EventController {

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

    PostToMarketplaceResponseProto.Builder resBuilder = PostToMarketplaceResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      UserEquip ue = UserEquipRetrieveUtils.getSpecificUserEquip(user.getId(), reqProto.getPostedEquipId());

      Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      Equipment equip = equipmentIdsToEquipment.get(ue.getEquipId());
      boolean legitPost = checkLegitPost(user, resBuilder, reqProto, diamondCost, coinCost, ue, equip);

      PostToMarketplaceResponseEvent resEvent = new PostToMarketplaceResponseEvent(senderProto.getUserId());
      resEvent.setPostToMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPost) {
        MarketplacePostType postType;
        if (diamondCost > 0) {
          postType = MarketplacePostType.PREMIUM_EQUIP_POST;
        } else {
          postType = MarketplacePostType.NORM_EQUIP_POST;
        }
        writeChangesToDB(user, reqProto, ue, postType);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in PostToMarketplaceController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegitPost(User user, Builder resBuilder, 
      PostToMarketplaceRequestProto reqProto, int diamondCost, int coinCost, UserEquip userEquip, Equipment equip) {
    if (user == null || equip == null) {
      resBuilder.setStatus(PostToMarketplaceStatus.OTHER_FAIL);
      return false;
    }
    if (userEquip == null || userEquip.getQuantity() < 1) {
      resBuilder.setStatus(PostToMarketplaceStatus.NOT_ENOUGH_EQUIP);
      return false;
    }
    if (user.getNumPostsInMarketplace() == ControllerConstants.POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER) {
      resBuilder.setStatus(PostToMarketplaceStatus.USER_ALREADY_MAX_MARKETPLACE_POSTS);
      return false;      
    }
    if (diamondCost < 0 || coinCost < 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NEGATIVE_COST);
      return false;
    }
    if (diamondCost > 0 && coinCost > 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.CANT_DEMAND_BOTH);
      return false;
    }
    if (diamondCost == 0 && coinCost == 0) {
      resBuilder.setStatus(PostToMarketplaceStatus.NO_COST);
      return false;
    }
    if (diamondCost > 0) {
      if (equip.getDiamondPrice() <= 0 && equip.getRarity() != Rarity.EPIC && equip.getRarity() != Rarity.LEGENDARY) {
        resBuilder.setStatus(PostToMarketplaceStatus.INVALID_COST_TYPE_FOR_POST);
        return true;        
      }
    }
    if (coinCost > 0) {
      if (equip.getCoinPrice() <= 0 && equip.getRarity() != Rarity.EPIC && equip.getRarity() != Rarity.LEGENDARY) {
        resBuilder.setStatus(PostToMarketplaceStatus.INVALID_COST_TYPE_FOR_POST);
        return true;        
      }
    }

    resBuilder.setStatus(PostToMarketplaceStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, PostToMarketplaceRequestProto reqProto, 
      UserEquip ue, MarketplacePostType postType) {

    if (ue != null) {
      if (!UpdateUtils.decrementUserEquip(user.getId(), reqProto.getPostedEquipId(), 
          ue.getQuantity(), 1)) {
        log.error("problem with updating user equips post-marketplace-post");
      }
    }

    int posterId = reqProto.getSender().getUserId();
    int postedEquipId = reqProto.getPostedEquipId();
    int diamondCost = reqProto.getDiamondCost();
    int coinCost = reqProto.getCoinCost();
    if (!InsertUtils.insertMarketplaceItem(posterId, postType, postedEquipId, 
        diamondCost, coinCost)) {
      log.error("problem with inserting into marketplace");      
    }
  } 

}