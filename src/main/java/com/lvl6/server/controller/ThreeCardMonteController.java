package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ThreeCardMonteRequestEvent;
import com.lvl6.events.response.ThreeCardMonteResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ThreeCardMonteRequestProto;
import com.lvl6.proto.EventProto.ThreeCardMonteResponseProto;
import com.lvl6.proto.EventProto.ThreeCardMonteResponseProto.Builder;
import com.lvl6.proto.EventProto.ThreeCardMonteResponseProto.ThreeCardMonteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class ThreeCardMonteController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ThreeCardMonteController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ThreeCardMonteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_THREE_CARD_MONTE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ThreeCardMonteRequestProto reqProto = ((ThreeCardMonteRequestEvent)event).getThreeCardMonteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    ThreeCardMonteResponseProto.Builder resBuilder = ThreeCardMonteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitPlay = checkLegitPlay(resBuilder, user);

      if (legitPlay) {
        chooseRewards(resBuilder);
      }
      ThreeCardMonteResponseEvent resEvent = new ThreeCardMonteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setThreeCardMonteResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPlay) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
      
    } catch (Exception e) {
      log.error("exception in ThreeCardMonte processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void chooseRewards(Builder resBuilder) {
    // TODO Auto-generated method stub
    
  }

  private boolean checkLegitPlay(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(ThreeCardMonteStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (user.getDiamonds() < ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY) {
      resBuilder.setStatus(ThreeCardMonteStatus.NOT_ENOUGH_DIAMONDS);
      log.error("buyer doesnt have enough diamonds. has " + user.getDiamonds() + ", needs " + ControllerConstants.THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY);
      return false;
    }
    resBuilder.setStatus(ThreeCardMonteStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user) {
    if (!user.updateRelativeNumGroupChatsRemainingAndDiamonds(ControllerConstants.PURCHASE_GROUP_CHAT__NUM_CHATS_GIVEN_FOR_PACKAGE, -1*ControllerConstants.PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE)) {
      log.error("problem with giving user more global chats and taking away diamonds");
    }
  }
}
