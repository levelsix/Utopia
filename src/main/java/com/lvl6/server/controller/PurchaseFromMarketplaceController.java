package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseFromMarketplaceRequestEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceRequestProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto.PurchaseFromMarketplaceStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PurchaseFromMarketplaceController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    Timestamp timeOfPurchaseRequest = new Timestamp(reqProto.getCurTime());
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

    if(server.lockPlayers(sellerId, buyerId, this.getClass().getSimpleName())) {
    try {
      MarketplacePost mp = MarketplacePostRetrieveUtils.getSpecificActiveMarketplacePost(postId);
      User buyer = RetrieveUtils.userRetrieveUtils().getUserById(buyerId);
      int previousSilverBuyer = 0;
      int previousGoldBuyer = 0;

      User seller = RetrieveUtils.userRetrieveUtils().getUserById(sellerId);        
      boolean legitPurchase = checkLegitPurchase(resBuilder, mp, buyer, seller, postId);

      PurchaseFromMarketplaceResponseEvent resEvent = new PurchaseFromMarketplaceResponseEvent(buyerId);
      resEvent.setTag(event.getTag());

      if (legitPurchase) {
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
        resBuilder.setSellerHadLicense(MiscMethods.validateMarketplaceLicense(seller, timeOfPurchaseRequest));
        
        int userEquipId = InsertUtils.get().insertUserEquip(buyer.getId(), mp.getPostedEquipId(), mp.getEquipLevel(),
            mp.getEquipEnhancementPercentage(), timeOfPurchaseRequest,
            ControllerConstants.UER__PURCHASE_FROM_MARKETPLACE);
        if (userEquipId < 0) {
          resBuilder.setStatus(PurchaseFromMarketplaceStatus.OTHER_FAIL);
          log.error("problem with giving 1 of equip " + mp.getPostedEquipId() + " to buyer " + buyer.getId());
          legitPurchase = false;
        } else {
          resBuilder.setFullUserEquipOfBoughtItem(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
              new UserEquip(userEquipId, buyer.getId(), mp.getPostedEquipId(), mp.getEquipLevel(), mp.getEquipEnhancementPercentage())));
        }
      }
      resEvent.setPurchaseFromMarketplaceResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitPurchase) {
        previousSilverBuyer = buyer.getCoins() + buyer.getVaultBalance();
        previousGoldBuyer = buyer.getDiamonds();
        
        PurchaseFromMarketplaceResponseEvent resEvent2 = new PurchaseFromMarketplaceResponseEvent(sellerId);
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
        resEvent2.setPurchaseFromMarketplaceResponseProto(resBuilder.build());  
        server.writeAPNSNotificationOrEvent(resEvent2);
        
        Map<String, Integer> moneyBuyer = new HashMap<String, Integer>();
        List<String> goldOrSilverTransaction = new ArrayList<String>(1);
        
        writeChangesToDB(buyer, seller, mp, timeOfPurchaseRequest, moneyBuyer, goldOrSilverTransaction);
        UpdateClientUserResponseEvent resEventUpdate;
        if (buyer != null && seller != null && mp != null) { //won't this always execute? ~Art
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(buyer);
          resEventUpdate.setTag(event.getTag());
          server.writeEvent(resEventUpdate);
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(seller);
          server.writeEvent(resEventUpdate);
          
          QuestUtils.checkAndSendQuestsCompleteBasic(server, buyer.getId(), senderProto, SpecialQuestAction.PURCHASE_FROM_MARKETPLACE, false);
        }
        writeToUserCurrencyHistory(mp, buyer, timeOfPurchaseRequest, moneyBuyer, 
            goldOrSilverTransaction, previousSilverBuyer, previousGoldBuyer);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseFromMarketplace processEvent", e);
    } finally {
      server.unlockPlayers(sellerId, buyerId, this.getClass().getSimpleName());      
    }
    }else {
    	log.warn("Unable to obtain lock in PurchaseFromMarketplaceController");
    }
  }


  private void writeChangesToDB(User buyer, User seller, MarketplacePost mp, Timestamp timeOfPurchaseRequest,
      Map<String, Integer> moneyBuyer, List<String> goldOrSilverTransaction) {
    if (seller == null || buyer == null || mp == null) {
      log.error("parameter passed in is null. seller=" + seller + ", buyer=" + buyer + ", post=" + mp);
    }
    int totalSellerDiamondChange = 0;
    int totalSellerCoinChange = 0;
    int totalBuyerDiamondChange = 0;
    int totalBuyerCoinChange = 0;
    
    boolean sellerHasLicense = MiscMethods.validateMarketplaceLicense(seller, timeOfPurchaseRequest);

    //MARKETPLACE LICENSE FEATURE:
    //if the seller has a license, he gets full amount of money, range is from 0 to 1
    double percentOfMoneyUserGets = 
    		1 - ControllerConstants.PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN;
    if(sellerHasLicense){
  	  percentOfMoneyUserGets = 1;
    }
    
    if (mp.getDiamondCost() > 0) {
      totalSellerDiamondChange += (int)Math.floor(percentOfMoneyUserGets*mp.getDiamondCost());
      totalBuyerDiamondChange -= mp.getDiamondCost();
      goldOrSilverTransaction.add(MiscMethods.gold);
    } else if (mp.getCoinCost() > 0) {
      totalSellerCoinChange += (int)Math.floor(percentOfMoneyUserGets*mp.getCoinCost());
      totalBuyerCoinChange -= mp.getCoinCost();    
      goldOrSilverTransaction.add(MiscMethods.silver);
    } else {
      log.error("marketplace post has no cost. mp=" + mp);
      return;
    }

    if (!DeleteUtils.get().deleteMarketplacePost(mp.getId())) {
      log.error("problem with deleting marketplace post with id " + mp.getId());      
    }

    boolean changeNumPostsInMarketplace = true;
    int numPostsInMarketplaceChange = MiscMethods.getNumPostsInMarketPlaceForUser(
        seller.getId());
    
    if (totalSellerDiamondChange != 0 || totalSellerCoinChange != 0) {
      if (!seller.isFake() && !seller.updateRelativeDiamondsearningsCoinsearningsNumpostsinmarketplaceNummarketplacesalesunredeemedNaive(
          totalSellerDiamondChange, totalSellerCoinChange, numPostsInMarketplaceChange, 1,
          changeNumPostsInMarketplace)) {
        log.error("problem with updating seller info. diamondChange=" + totalSellerDiamondChange
            + ", coinChange=" + totalSellerCoinChange + ", num posts in marketplace decremented by 1, " +
            		"num marketplace sales unredeemed increased by 1");
      }
    }
    changeNumPostsInMarketplace = false;
    if (totalBuyerDiamondChange != 0 || totalBuyerCoinChange != 0)
    {
      if (!buyer.updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive(totalBuyerDiamondChange, 
          totalBuyerCoinChange, 0, changeNumPostsInMarketplace)) {
        log.error("problem with updating buyer info. diamondChange=" + totalBuyerDiamondChange
            + ", coinChange=" + totalBuyerCoinChange);
      } else {
        //things went ok
        moneyBuyer.put(MiscMethods.gold, totalBuyerDiamondChange);
        moneyBuyer.put(MiscMethods.silver, totalBuyerCoinChange);
      }
    }

    if (!InsertUtils.get().insertMarketplaceItemIntoHistory(mp, buyer.getId(), sellerHasLicense, timeOfPurchaseRequest)) {
      log.error("problem with adding to marketplace history the post " + mp + " with buyer " + buyer.getId());
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
  
  //only gold changes or silver changes, not both, seller doesn't really get the money until seller redeems purchase
  private void writeToUserCurrencyHistory(MarketplacePost mp, User buyer, Timestamp date, 
      Map<String, Integer> moneyBuyerCurrencyChange, List<String> goldOrSilverTransaction,
      int previousSilver, int previousGold) {
    if(goldOrSilverTransaction.isEmpty()) {
      return;
    }
    try {
      String goldOrSilver = goldOrSilverTransaction.get(0); 
      int mpId = mp.getId();
      int userId = buyer.getId();
      int isSilver = 0;
      int currencyChange = moneyBuyerCurrencyChange.get(goldOrSilver);
      int currencyBefore = 0;
      int currencyAfter = 0;
      String reasonForChange = ControllerConstants.UCHRFC__PURCHASED_FROM_MARKETPLACE + " marketplace_id:" + mpId;
      
      if(goldOrSilver.equals(MiscMethods.gold)) {
        //not a silver change but gold change
        currencyAfter = buyer.getDiamonds();
        currencyBefore = previousGold;
      } else {
        isSilver = 1;
        currencyAfter = buyer.getCoins() + buyer.getVaultBalance();
        currencyBefore = previousSilver;
      }

      InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver, 
          currencyChange, currencyBefore, currencyAfter, reasonForChange);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
  
}
