package com.lvl6.server.controller;


import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseFromMarketplaceRequestEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceRequestProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.PurchaseFromMarketplaceStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class PurchaseFromMarketplaceController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
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
      User buyer = UserRetrieveUtils.getUserById(buyerId);

      User seller = UserRetrieveUtils.getUserById(sellerId);        
      boolean legitPurchase = checkLegitPurchase(resBuilder, mp, buyer, seller);

      PurchaseFromMarketplaceResponseEvent resEvent = new PurchaseFromMarketplaceResponseEvent(buyerId);
      resEvent.setTag(event.getTag());

      if (legitPurchase)
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
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
          
          QuestUtils.checkAndSendQuestsCompleteBasic(server, buyer.getId(), senderProto, 
              null, null, null, mp.getPostedEquipId(), 1);
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
      log.error("problem with retracting marketplace post");
    }
    int totalSellerDiamondChange = 0;
    int totalSellerCoinChange = 0;
    int totalBuyerDiamondChange = 0;
    int totalBuyerCoinChange = 0;

    if (mp.getDiamondCost() > 0) {
      totalSellerDiamondChange += (int)Math.floor((1-ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)*mp.getDiamondCost());
      totalBuyerDiamondChange -= mp.getDiamondCost();
    }
    if (mp.getCoinCost() > 0) {
      totalSellerCoinChange += (int)Math.floor((1-ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN)*mp.getCoinCost());
      totalBuyerCoinChange -= mp.getCoinCost();      
    }

    if (totalSellerDiamondChange != 0 || totalSellerCoinChange != 0) {
      if (!seller.updateRelativeDiamondsearningsCoinsearningsNumpostsinmarketplaceNummarketplacesalesunredeemedNaive(totalSellerDiamondChange, totalSellerCoinChange, -1, 1)) {
        log.error("problem with giving seller postmarketplace results");
      }
    }
    if (totalBuyerDiamondChange != 0 || totalBuyerCoinChange != 0)
    {
      if (!buyer.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(totalBuyerDiamondChange, totalBuyerCoinChange, 0)) {
        log.error("problem with giving buyer postmarketplace results");
      }
    }

    if (!UpdateUtils.incrementUserEquip(buyer.getId(), mp.getPostedEquipId(), 1)) {
      log.error("problem with giving buyer marketplace equip");
    }

    if (!InsertUtils.insertMarketplaceItemIntoHistory(mp, buyer.getId())) {
      log.error("problem with adding to marketplace history");            
    }

    if (!DeleteUtils.deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post");      
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, MarketplacePost mp, User buyer, User seller) {
    if (mp == null) {
      resBuilder.setStatus(PurchaseFromMarketplaceStatus.POST_NO_LONGER_EXISTS);
      return false;
    }
    if (buyer == null || seller == null || seller.getId() != mp.getPosterId()) {
      resBuilder.setStatus(PurchaseFromMarketplaceStatus.OTHER_FAIL);
      return false;      
    }
    if (mp.getDiamondCost() > 0) {
      if (buyer.getDiamonds() < mp.getDiamondCost()) {
        resBuilder.setStatus(PurchaseFromMarketplaceStatus.NOT_ENOUGH_MATERIALS);
        return false;
      }
    }
    if (mp.getCoinCost() > 0) {
      if (buyer.getCoins() < mp.getCoinCost()) {
        resBuilder.setStatus(PurchaseFromMarketplaceStatus.NOT_ENOUGH_MATERIALS);
        return false;
      }
    }
    resBuilder.setStatus(PurchaseFromMarketplaceStatus.SUCCESS);
    return true;
  }
}
