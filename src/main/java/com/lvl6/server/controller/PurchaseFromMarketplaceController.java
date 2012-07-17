package com.lvl6.server.controller;


import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseFromMarketplaceRequestEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceRequestProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.PurchaseFromMarketplaceStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PurchaseFromMarketplaceController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PurchaseFromMarketplaceController() {
    numAllocatedThreads = 4;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseFromMarketplaceRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_FROM_MARKETPLACE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseFromMarketplaceRequestProto reqProto = ((PurchaseFromMarketplaceRequestEvent)event).getPurchaseFromMarketplaceRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int postId = reqProto.getMarketplacePostId();
    int sellerId = reqProto.getPosterId();
    int buyerId = senderProto.getUserId();

    PurchaseFromMarketplaceResponseProto.Builder resBuilder = PurchaseFromMarketplaceResponseProto.newBuilder();
    resBuilder.setPurchaser(senderProto);
    resBuilder.setPosterId(sellerId);

    if (buyerId == sellerId) {
      resBuilder.setStatus(PurchaseFromMarketplaceStatus.PURCHASER_IS_SELLER);
      PurchaseFromMarketplaceResponseEvent resEvent = new PurchaseFromMarketplaceResponseEvent(buyerId);
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseFromMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      return;
    }

    server.lockPlayers(sellerId, buyerId);
    try {
      MarketplacePost mp = MarketplacePostRetrieveUtils.getSpecificActiveMarketplacePost(postId);
      User buyer = RetrieveUtils.userRetrieveUtils().getUserById(buyerId);

      User seller = RetrieveUtils.userRetrieveUtils().getUserById(sellerId);        
      boolean legitPurchase = checkLegitPurchase(resBuilder, mp, buyer, seller, postId);

      PurchaseFromMarketplaceResponseEvent resEvent = new PurchaseFromMarketplaceResponseEvent(buyerId);
      resEvent.setTag(event.getTag());

      if (legitPurchase) {
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
        
        int userEquipId = InsertUtils.get().insertUserEquip(buyer.getId(), mp.getPostedEquipId(), mp.getEquipLevel());
        if (userEquipId < 0) {
          resBuilder.setStatus(PurchaseFromMarketplaceStatus.OTHER_FAIL);
          log.error("problem with giving 1 of equip " + mp.getPostedEquipId() + " to buyer " + buyer.getId());
          legitPurchase = false;
        } else {
          resBuilder.setFullUserEquipOfBoughtItem(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
              new UserEquip(userEquipId, buyer.getId(), mp.getPostedEquipId(), mp.getEquipLevel())));
        }
      }
      resEvent.setPurchaseFromMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitPurchase) {
        PurchaseFromMarketplaceResponseEvent resEvent2 = new PurchaseFromMarketplaceResponseEvent(sellerId);
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
        resEvent2.setPurchaseFromMarketplaceResponseProto(resBuilder.build());  
        server.writeAPNSNotificationOrEvent(resEvent2);
        
        writeChangesToDB(buyer, seller, mp);
        UpdateClientUserResponseEvent resEventUpdate;
        if (buyer != null && seller != null && mp != null) {
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(buyer);
          resEventUpdate.setTag(event.getTag());
          server.writeEvent(resEventUpdate);
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(seller);
          server.writeEvent(resEventUpdate);
          
          QuestUtils.checkAndSendQuestsCompleteBasic(server, buyer.getId(), senderProto, SpecialQuestAction.PURCHASE_FROM_MARKETPLACE, false);
        }
      }
    } catch (Exception e) {
      log.error("exception in PurchaseFromMarketplace processEvent", e);
    } finally {
      server.unlockPlayers(sellerId, buyerId);      
    }
  }


  private void writeChangesToDB(User buyer, User seller, MarketplacePost mp) {
    if (seller == null || buyer == null || mp == null) {
      log.error("parameter passed in is null. seller=" + seller + ", buyer=" + buyer + ", post=" + mp);
    }
    int totalSellerDiamondChange = 0;
    int totalSellerCoinChange = 0;
    int totalBuyerDiamondChange = 0;
    int totalBuyerCoinChange = 0;

    if (mp.getDiamondCost() > 0) {
      totalSellerDiamondChange += (int)Math.floor((1-ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)*mp.getDiamondCost());
      totalBuyerDiamondChange -= mp.getDiamondCost();
    } else if (mp.getCoinCost() > 0) {
      totalSellerCoinChange += (int)Math.floor((1-ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)*mp.getCoinCost());
      totalBuyerCoinChange -= mp.getCoinCost();      
    } else {
      log.error("marketplace post has no cost. mp=" + mp);
      return;
    }

    if (totalSellerDiamondChange != 0 || totalSellerCoinChange != 0) {
      if (!seller.isFake() && !seller.updateRelativeDiamondsearningsCoinsearningsNumpostsinmarketplaceNummarketplacesalesunredeemedNaive(
          totalSellerDiamondChange, totalSellerCoinChange, -1, 1)) {
        log.error("problem with updating seller info. diamondChange=" + totalSellerDiamondChange
            + ", coinChange=" + totalSellerCoinChange + ", num posts in marketplace decremented by 1, " +
            		"num marketplace sales unredeemed increased by 1");
      }
    }
    if (totalBuyerDiamondChange != 0 || totalBuyerCoinChange != 0)
    {
      if (!buyer.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(totalBuyerDiamondChange, totalBuyerCoinChange, 0)) {
        log.error("problem with updating buyer info. diamondChange=" + totalBuyerDiamondChange
            + ", coinChange=" + totalBuyerCoinChange);
      }
    }

//    UserEquip userEquip = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquip(buyer.getId(), mp.getPostedEquipId());
    Equipment equipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(mp.getPostedEquipId()); 
    if (equipment == null) {
      log.error("equipment with " + mp.getPostedEquipId() + " does not exist");
    } 
//    else if ((userEquip == null || userEquip.getQuantity() < 1) && equipment != null) {
//      if (MiscMethods.checkIfEquipIsEquippableOnUser(equipment, buyer) && !buyer.updateEquipped(equipment)) {
//        log.error("problem with equipping " + equipment + " for user " + buyer);
//      }
//    }
    
    if (!InsertUtils.get().insertMarketplaceItemIntoHistory(mp, buyer.getId())) {
      log.error("problem with adding to marketplace history the post " + mp + " with buyer " + buyer.getId());
    }

    if (!DeleteUtils.get().deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post with id " + mp.getId());      
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, MarketplacePost mp, User buyer, User seller, int postId) {
    if (mp == null) {
      resBuilder.setStatus(PurchaseFromMarketplaceStatus.POST_NO_LONGER_EXISTS);
      log.warn("post that user tried to buy no longer exists. post id is " + postId);
      return false;
    }
    if (buyer == null || seller == null || seller.getId() != mp.getPosterId()) {
      resBuilder.setStatus(PurchaseFromMarketplaceStatus.OTHER_FAIL);
      log.error("parameter passed in is null, or seller is not the right poster. buyer=" + buyer + ", seller=" + seller
          + ", posterId=" + mp.getPosterId());
      return false;      
    }
    if (mp.getDiamondCost() > 0) {
      if (buyer.getDiamonds() < mp.getDiamondCost()) {
        resBuilder.setStatus(PurchaseFromMarketplaceStatus.NOT_ENOUGH_MATERIALS);
        log.error("buyer doesnt have enough diamonds. has " + buyer.getDiamonds() + ", needs " + mp.getDiamondCost());
        return false;
      }
    }
    if (mp.getCoinCost() > 0) {
      if (buyer.getCoins() < mp.getCoinCost()) {
        resBuilder.setStatus(PurchaseFromMarketplaceStatus.NOT_ENOUGH_MATERIALS);
        log.error("buyer doesnt have enough coins. has " + buyer.getCoins() + ", needs " + mp.getCoinCost());
        return false;
      }
    }
    resBuilder.setStatus(PurchaseFromMarketplaceStatus.SUCCESS);
    return true;
  }
}
