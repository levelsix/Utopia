package com.lvl6.server.controller;

import org.apache.log4j.Logger;
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

@Component @DependsOn("gameServer") public class PurchaseGroupChatController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

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
      }
    } catch (Exception e) {
      log.error("exception in PurchaseGroupChat processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
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
}
