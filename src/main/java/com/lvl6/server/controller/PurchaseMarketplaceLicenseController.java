package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseMarketplaceLicenseRequestEvent;
import com.lvl6.events.response.PurchaseMarketplaceLicenseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseRequestProto;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseRequestProto.LicenseType;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseResponseProto;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseResponseProto.PurchaseMarketplaceLicenseStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

/*
 * NOT READY/BEING USED YET
 */

  @Component @DependsOn("gameServer") public class PurchaseMarketplaceLicenseController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PurchaseMarketplaceLicenseController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseMarketplaceLicenseRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_MARKETPLACE_LICENSE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseMarketplaceLicenseRequestProto reqProto = ((PurchaseMarketplaceLicenseRequestEvent)event).getPurchaseMarketplaceLicenseRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp timeOfPurchase = new Timestamp(reqProto.getClientTime());
    LicenseType type = reqProto.getLicenseType();

    PurchaseMarketplaceLicenseResponseProto.Builder resBuilder = PurchaseMarketplaceLicenseResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousGold = user.getDiamonds();
      boolean legitPurchase = checkLegitPurchase(resBuilder, user, timeOfPurchase, type);

      PurchaseMarketplaceLicenseResponseEvent resEvent = new PurchaseMarketplaceLicenseResponseEvent(senderProto.getUserId());
      resEvent.setPurchaseMarketplaceLicenseResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPurchase) {
        writeChangesToDB(user, type, timeOfPurchase);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        writeToUserCurrencyHistory(user, type, timeOfPurchase,
            previousGold); //don't want to hold up thread from sending to client, hence after writeEvent
      }
    } catch (Exception e) {
      log.error("exception in PurchaseMarketplaceLicense processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, LicenseType type, Timestamp timeOfPurchase) {
    if (type == LicenseType.SHORT) {
      if (!user.updateRelativeDiamondsAbsoluteLastshortlicensepurchasetimeLastlonglicensepurchasetime
          (ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST*-1, timeOfPurchase, null)) {
        log.error("problem with giving user marketplace license");
      }
    } else if (type == LicenseType.LONG) {
      if (!user.updateRelativeDiamondsAbsoluteLastshortlicensepurchasetimeLastlonglicensepurchasetime
          (ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST*-1, null, timeOfPurchase)) {
        log.error("problem with giving user marketplace license");
      }
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, User user, Timestamp timeOfPurchase, LicenseType type) {
    if (user == null || timeOfPurchase == null || type == null) {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    }

    int diamondCost;
    if (type == LicenseType.SHORT) {
      diamondCost = ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST;
    } else if (type == LicenseType.LONG) {
      diamondCost = ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST;
    } else {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.OTHER_FAIL);
      return false;
    }

    if (user.getDiamonds() < diamondCost) {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }

    if (user.getLastShortLicensePurchaseTime() != null && user.getLastShortLicensePurchaseTime().getTime() + 
        86400000*ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE >= timeOfPurchase.getTime()) {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.ALREADY_HAVE_LICENSE_NOW);
      return false;
    }
    if (user.getLastLongLicensePurchaseTime() != null && user.getLastLongLicensePurchaseTime().getTime() + 
        86400000*ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE >= timeOfPurchase.getTime()) {
      resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.ALREADY_HAVE_LICENSE_NOW);
      return false;
    }

    resBuilder.setStatus(PurchaseMarketplaceLicenseStatus.SUCCESS);
    return true;
  }
  
  private void writeToUserCurrencyHistory(User aUser, LicenseType type, Timestamp timeOfPurchase,
      int previousGold) {
    Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String reasonForChange = "";

    int currencyChange = 0;
    
    if (LicenseType.SHORT == type) {
      currencyChange = ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST*-1;
      reasonForChange = ControllerConstants.UCHRFC__SHORT_MARKET_PLACE_LICENSE;
    } else if (LicenseType.LONG == type) {
      currencyChange = ControllerConstants.PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST*-1;
      reasonForChange = ControllerConstants.UCHRFC__LONG_MARKET_PLACE_LICENSE;
    } else {
      log.error("wrong LicenseType. LicenseType=" + type);
      return;
    }
    
    goldSilverChange.put(MiscMethods.gold, currencyChange);
    previousGoldSilver.put(MiscMethods.gold, previousGold);
    reasonsForChanges.put(MiscMethods.gold, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, timeOfPurchase, goldSilverChange, previousGoldSilver, reasonsForChanges);
  }

}
