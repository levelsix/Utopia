package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SendGroupChatRequestEvent;
import com.lvl6.events.response.SendGroupChatResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.SendGroupChatRequestProto;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto.Builder;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto.SendGroupChatStatus;
import com.lvl6.proto.InfoProto.GroupChatScope;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class SendGroupChatController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public SendGroupChatController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new SendGroupChatRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SEND_GROUP_CHAT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    SendGroupChatRequestProto reqProto = ((SendGroupChatRequestEvent)event).getSendGroupChatRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    GroupChatScope scope = reqProto.getScope();
    String chatMessage = reqProto.getChatMessage();
    
    SendGroupChatResponseProto.Builder resBuilder = SendGroupChatResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitSend = checkLegitSend(resBuilder, user, scope, chatMessage);

      SendGroupChatResponseEvent resEvent = new SendGroupChatResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setSendGroupChatResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitSend) {
        writeChangesToDB(user);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        //TODO: POST THE MESSAGE
        
      }
    } catch (Exception e) {
      log.error("exception in SendGroupChat processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user) {
    if (!user.updateRelativeNumGroupChatsRemainingAndDiamonds(-1, 0)) {
      log.error("problem with decrementing a global chat");
    }
  }
  
  private boolean checkLegitSend(Builder resBuilder, User user, GroupChatScope scope, String chatMessage) {
    if (user == null || scope == null || chatMessage == null || chatMessage.length() == 0) {
      resBuilder.setStatus(SendGroupChatStatus.OTHER_FAIL);
      log.error("user is " + user + ", scope is " + scope + ", chatMessage=" + chatMessage);
      return false;      
    }
    
    boolean isAlliance = MiscMethods.checkIfGoodSide(user.getType());
    if ((scope == GroupChatScope.ALLIANCE && !isAlliance) || (scope == GroupChatScope.LEGION || isAlliance)) {
      resBuilder.setStatus(SendGroupChatStatus.WRONG_SIDE);
      log.error("user type is " + user.getType() + ", scope is " + scope);
      return false;      
    }
    
    if (user.getNumGroupChatsRemaining() <= 0) {
      resBuilder.setStatus(SendGroupChatStatus.NOT_ENOUGH_GROUP_CHATS);
      log.error("user has no group chats remaining");
      return false;      
    }
    
    if (chatMessage.length() > ControllerConstants.SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING) {
      resBuilder.setStatus(SendGroupChatStatus.TOO_LONG);
      log.error("chat message is too long. allowed is " + ControllerConstants.SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING
          + ", length is " + chatMessage.length() + ", chatMessage is " + chatMessage);
      return false;      
    }
    
    resBuilder.setStatus(SendGroupChatStatus.SUCCESS);
    return true;
  }
}
