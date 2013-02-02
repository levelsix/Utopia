package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseGroupChatRequestEvent;
import com.lvl6.events.response.PurchaseGroupChatResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PurchaseGroupChatRequestProto;
import com.lvl6.proto.EventProto.PurchaseGroupChatResponseProto;
import com.lvl6.proto.EventProto.PurchaseGroupChatResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseGroupChatResponseProto.PurchaseGroupChatStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class PurchaseGroupChatController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PurchaseGroupChatController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseGroupChatRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_GROUP_CHAT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PurchaseGroupChatRequestProto reqProto = ((PurchaseGroupChatRequestEvent)event).getPurchaseGroupChatRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    PurchaseGroupChatResponseProto.Builder resBuilder = PurchaseGroupChatResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousGold = user.getDiamonds();
      boolean legitPurchase = checkLegitPurchase(resBuilder, user);

      PurchaseGroupChatResponseEvent resEvent = new PurchaseGroupChatResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPurchaseGroupChatResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPurchase) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        writeToUserCurrencyHistory(user, previousGold); //don't want to hold up thread from sending to client, hence after writeEvent
      }
    } catch (Exception e) {
      log.error("exception in PurchaseGroupChat processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private void writeChangesToDB(User user) {
    if (!user.updateRelativeNumGroupChatsRemainingAndDiamonds(ControllerConstants.PURCHASE_GROUP_CHAT__NUM_CHATS_GIVEN_FOR_PACKAGE, -1*ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE)) {
      log.error("problem with giving user more global chats and taking away diamonds");
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(PurchaseGroupChatStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (user.getDiamonds() < ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE) {
      resBuilder.setStatus(PurchaseGroupChatStatus.NOT_ENOUGH_DIAMONDS);
      log.error("buyer doesnt have enough diamonds. has " + user.getDiamonds() + ", needs " + ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE);
      return false;
    }
    resBuilder.setStatus(PurchaseGroupChatStatus.SUCCESS);
    return true;
  }
  
  private void writeToUserCurrencyHistory(User u, int previousGold) {
    //try, catch just a precaution
    try {
      Timestamp date = new Timestamp((new Date()).getTime());
      int isSilver = 0;
      int currencyChange = -1 * ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE;
      int currencyAfter = u.getDiamonds();
      //int currencyBefore = currencyAfter - currencyChange;
      String reasonForChange = ControllerConstants.UCHRFC__GROUP_CHAT;
    
      InsertUtils.get().insertIntoUserCurrencyHistory(u.getId(), date, isSilver,
          currencyChange, previousGold, currencyAfter, reasonForChange);
    
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? " + e.toString());
    }
  }
}
