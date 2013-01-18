package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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

    if(server.lockPlayers(sellerId, buyerId)) {
    try {
      MarketplacePost mp = MarketplacePostRetrieveUtils.getSpecificActiveMarketplacePost(postId);
      User buyer = RetrieveUtils.userRetrieveUtils().getUserById(buyerId);

      User seller = RetrieveUtils.userRetrieveUtils().getUserById(sellerId);        
      boolean legitPurchase = checkLegitPurchase(resBuilder, mp, buyer, seller, postId);

      PurchaseFromMarketplaceResponseEvent resEvent = new PurchaseFromMarketplaceResponseEvent(buyerId);
      resEvent.setTag(event.getTag());

      if (legitPurchase) {
        resBuilder.setMarketplacePost(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp, seller));
        resBuilder.setSellerHadLicense(MiscMethods.validateMarketplaceLicense(seller, timeOfPurchaseRequest));
        
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
        
        Map<String, Integer> moneyBuyer = new HashMap<String, Integer>();
        Map<String, Integer> moneySeller = new HashMap<String, Integer>();
        List<String> goldOrSilverTransaction = new ArrayList<String>(1);
        
        writeChangesToDB(buyer, seller, mp, timeOfPurchaseRequest, moneyBuyer, moneySeller, goldOrSilverTransaction);
        UpdateClientUserResponseEvent resEventUpdate;
        if (buyer != null && seller != null && mp != null) { //won't this always execute? ~Art
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(buyer);
          resEventUpdate.setTag(event.getTag());
          server.writeEvent(resEventUpdate);
          resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(seller);
          server.writeEvent(resEventUpdate);
          
          QuestUtils.checkAndSendQuestsCompleteBasic(server, buyer.getId(), senderProto, SpecialQuestAction.PURCHASE_FROM_MARKETPLACE, false);
        }
        writeToUserCurrencyHistory(buyer, seller, timeOfPurchaseRequest, moneyBuyer, moneySeller, goldOrSilverTransaction);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseFromMarketplace processEvent", e);
    } finally {
      server.unlockPlayers(sellerId, buyerId);      
    }
    }else {
    	log.warn("Unable to obtain lock in PurchaseFromMarketplaceController");
    }
  }


  private void writeChangesToDB(User buyer, User seller, MarketplacePost mp, Timestamp timeOfPurchaseRequest,
      Map<String, Integer> moneyBuyer, Map<String, Integer> moneySeller, List<String> goldOrSilverTransaction) {
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
      } else {
        //things went ok
        moneySeller.put(MiscMethods.gold, totalSellerDiamondChange);
        moneySeller.put(MiscMethods.silver, totalSellerCoinChange);
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

    if (!InsertUtils.get().insertMarketplaceItemIntoHistory(mp, buyer.getId(), sellerHasLicense)) {
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
  
  //only gold changes or silver changes, not both
  private void writeToUserCurrencyHistory(User buyer, User seller, Timestamp date, Map<String, Integer> moneyBuyer, 
      Map<String, Integer> moneySeller, List<String> goldOrSilverTransaction) {
    if(goldOrSilverTransaction.isEmpty()) {
      return;
    }
    try {
      String goldOrSilver = goldOrSilverTransaction.get(0); 
      int amount = 2;
      List<Integer> userIds = new ArrayList<Integer>(amount);
      List<Timestamp> dates = new ArrayList<Timestamp>(Collections.nCopies(amount, date));
      List<Integer> areSilver;
      List<Integer> currenciesChange = new ArrayList<Integer>(amount);
      List<Integer> currenciesBefore = new ArrayList<Integer>(amount);
      List<String> reasonsForChanges = new ArrayList<String>(amount);
      
      //buyer information then seller's
      userIds.add(buyer.getId());
      userIds.add(seller.getId());
      
      int buyerCurrencyChange = moneyBuyer.get(goldOrSilver);
      int sellerCurrencyChange = moneySeller.get(goldOrSilver);
      
      
      if(MiscMethods.gold == goldOrSilver) {
        //not a silver change but gold change
        areSilver = new ArrayList<Integer>(Collections.nCopies(amount, 0));
        
        int buyerGoldBeforeChange = buyer.getDiamonds() - buyerCurrencyChange;
        int sellerGoldBeforeChange = seller.getDiamonds() - sellerCurrencyChange;
        currenciesBefore.add(buyerGoldBeforeChange);
        currenciesBefore.add(sellerGoldBeforeChange);
      } else {
        areSilver = new ArrayList<Integer>(Collections.nCopies(amount, 1));
        
        int buyerSilverBeforeChange = buyer.getCoins() - buyerCurrencyChange;
        int sellerSilverBeforeChange = seller.getCoins() - sellerCurrencyChange;
        currenciesBefore.add(buyerSilverBeforeChange);
        currenciesBefore.add(sellerSilverBeforeChange);
      }
      
      currenciesChange.add(buyerCurrencyChange);
      currenciesChange.add(sellerCurrencyChange);
      reasonsForChanges.add(ControllerConstants.UCHRFC__SOLD_ITEM_ON_MARKETPLACE); //buyer reason
      reasonsForChanges.add(ControllerConstants.UCHRFC__PURCHASED_FROM_MARKETPLACE); //seller reason

      int numInserted = InsertUtils.get().insertIntoUserCurrencyHistoryMultipleRows(userIds, dates, areSilver,
          currenciesChange, currenciesBefore, reasonsForChanges);
      log.info("Should be 2. Rows inserted into user_currency_history: " + numInserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
    
  }
  
}
