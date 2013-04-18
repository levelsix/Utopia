package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseForgeSlotRequestEvent;
import com.lvl6.events.response.PurchaseForgeSlotResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseForgeSlotRequestProto;
import com.lvl6.proto.EventProto.PurchaseForgeSlotResponseProto;
import com.lvl6.proto.EventProto.PurchaseForgeSlotResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseForgeSlotResponseProto.PurchaseForgeSlotStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;


  @Component @DependsOn("gameServer") public class PurchaseForgeSlotController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  
  public PurchaseForgeSlotController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseForgeSlotRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_FORGE_SLOT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseForgeSlotRequestProto reqProto = ((PurchaseForgeSlotRequestEvent)event).getPurchaseForgeSlotRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    
    
    PurchaseForgeSlotResponseProto.Builder resBuilder = PurchaseForgeSlotResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousGold = 0;
      
      boolean legitPurchase = checkLegitPurchase(resBuilder, user);
      

      int userAdditionalForgeSlots = 0;
      int goalNumAdditionalForgeSlots = 1;
      
      Map<String, Integer> currencyChange = new HashMap<String, Integer>();
      boolean success = false;
      if (legitPurchase) {
        previousGold = user.getDiamonds();
        
        userAdditionalForgeSlots = user.getNumAdditionalForgeSlots();
        goalNumAdditionalForgeSlots = userAdditionalForgeSlots + 1;
        int additionalForgeSlotCost = MiscMethods.costToBuyForgeSlot(goalNumAdditionalForgeSlots, userAdditionalForgeSlots);
        int diamondCost = additionalForgeSlotCost * -1;
        
        success = writeChangesToDB(resBuilder, user, userAdditionalForgeSlots, 
            goalNumAdditionalForgeSlots, diamondCost, currencyChange);
      }
      
      PurchaseForgeSlotResponseEvent resEvent = new PurchaseForgeSlotResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseForgeSlotResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
       
      if (success) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        writeToUserCurrencyHistory(user, userAdditionalForgeSlots, goalNumAdditionalForgeSlots, currencyChange, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in PurchaseForgeSlot processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean writeChangesToDB(Builder resBuilder, User user, int currentAdditionalForgeSlots, 
      int goalNumAdditionalForgeSlots, int diamondCost, Map<String, Integer> currencyChange) {
    
    if (!user.updateNumAdditionalForgeSlotsAndDiamonds(goalNumAdditionalForgeSlots, diamondCost)) {
      log.error("problem with updating diamonds after purchasing additional forge slot");
      resBuilder.setStatus(PurchaseForgeSlotStatus.FAIL_OTHER);
      return false;
    } else {//everything went ok
      currencyChange.put(MiscMethods.gold, diamondCost);
      resBuilder.setStatus(PurchaseForgeSlotStatus.SUCCESS);
      return true;
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(PurchaseForgeSlotStatus.FAIL_OTHER);
      return false;
    }
    
    //see if user wants to go past max slots
    int userAdditionalForgeSlots = user.getNumAdditionalForgeSlots();
    int maxAdditionalForgeSlots = ControllerConstants.FORGE__ADDITIONAL_MAX_FORGE_SLOTS;
    if (userAdditionalForgeSlots == maxAdditionalForgeSlots) {
      resBuilder.setStatus(PurchaseForgeSlotStatus.FAIL_ALREADY_AT_MAX_FORGE_SLOTS);
      log.error("user error: user trying to buy past max additional forge slots." +
      		" maxAdditionalForgeSlots=" + maxAdditionalForgeSlots + ", user=" + user);
      return false;      
    }
    //see if user is at max slots
    if (userAdditionalForgeSlots > maxAdditionalForgeSlots) {
      resBuilder.setStatus(PurchaseForgeSlotStatus.FAIL_USER_HAS_MORE_THAN_MAX_FORGE_SLOTS);
      log.error("user error: user already has more than max additional forge slots." +
      		" maxAdditionalForgeSlots=" + maxAdditionalForgeSlots + ", user=" + user);
      return false;      
    }
    
    //see if user has enough money
    int goalNumAdditionalForgeSlots = userAdditionalForgeSlots + 1;
    int additionalForgeSlotCost = MiscMethods.costToBuyForgeSlot(goalNumAdditionalForgeSlots, userAdditionalForgeSlots);
    
    if (user.getDiamonds() < additionalForgeSlotCost) {
      resBuilder.setStatus(PurchaseForgeSlotStatus.FAIL_NOT_ENOUGH_GOLD);
      log.error("user error: user does not have enough gold to buy one more forge slot."
          + " cost=" + additionalForgeSlotCost + ", goalNumAdditionalForgeSlots=" + goalNumAdditionalForgeSlots
          + " user=" + user);
      return false;
    }
    
    return true;  
  }
  
  public void writeToUserCurrencyHistory(User aUser, int previousForgeSlots, int currentAdditionalForgeSlots,
      Map<String, Integer> goldSilverChange, int previousSilver) {
    Timestamp date = new Timestamp((new Date()).getTime());
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String reasonForChange = 
        ControllerConstants.UCHRFC__PURCHASED_ADDITIONAL_FORGE_SLOTS +
        " prevAmount=" + previousForgeSlots + ", currentAmount=" + currentAdditionalForgeSlots; 
        
    
    previousGoldSilver.put(gold, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, goldSilverChange,
        previousGoldSilver, reasonsForChanges);
  }
}
