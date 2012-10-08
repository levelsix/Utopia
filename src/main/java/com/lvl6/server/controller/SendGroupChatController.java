package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SendGroupChatRequestEvent;
import com.lvl6.events.response.ReceivedGroupChatResponseEvent;
import com.lvl6.events.response.SendGroupChatResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ReceivedGroupChatResponseProto;
import com.lvl6.proto.EventProto.SendGroupChatRequestProto;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto.Builder;
import com.lvl6.proto.EventProto.SendGroupChatResponseProto.SendGroupChatStatus;
import com.lvl6.proto.InfoProto.GroupChatScope;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component
@DependsOn("gameServer")
public class SendGroupChatController extends EventController {


  private static Logger log = Logger.getLogger(new Object() {
  }.getClass().getEnclosingClass());

  @Resource(name="outgoingGameEventsHandlerExecutor")
  protected TaskExecutor executor;


  public TaskExecutor getExecutor() {
    return executor;
  }

  public void setExecutor(TaskExecutor executor) {
    this.executor = executor;
  }

  @Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;

  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
    return playersByPlayerId;
  }

  public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
    this.playersByPlayerId = playersByPlayerId;
  }

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
  protected void processRequestEvent(final RequestEvent event) throws Exception {
    final SendGroupChatRequestProto reqProto = ((SendGroupChatRequestEvent) event)
        .getSendGroupChatRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    final GroupChatScope scope = reqProto.getScope();
    String chatMessage = reqProto.getChatMessage();


    SendGroupChatResponseProto.Builder resBuilder = SendGroupChatResponseProto
        .newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      final User user = RetrieveUtils.userRetrieveUtils().getUserById(
          senderProto.getUserId());

      boolean legitSend = checkLegitSend(resBuilder, user, scope,
          chatMessage);

      SendGroupChatResponseEvent resEvent = new SendGroupChatResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setSendGroupChatResponseProto(resBuilder.build());
      server.writeEvent(resEvent);

      if (legitSend) {
//        if (scope == GroupChatScope.GLOBAL) {
//          writeChangesToDB(user);
//        }
        
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods
            .createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        final ReceivedGroupChatResponseProto.Builder chatProto = ReceivedGroupChatResponseProto.newBuilder();
        chatProto.setChatMessage(chatMessage);
        chatProto.setSender(senderProto);
        chatProto.setScope(scope);
        //send messages in background so sending player can unlock
        executor.execute(new Runnable(){
          @Override
          public void run() {
            sendChatMessageToConnectedPlayers(chatProto, event.getTag(), scope == GroupChatScope.CLAN, user.getClanId());
          }
        });
      }
    } catch (Exception e) {
      log.error("exception in SendGroupChat processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }


  protected void sendChatMessageToConnectedPlayers(ReceivedGroupChatResponseProto.Builder chatProto, int tag, boolean forClan, int clanId) {
    Collection<ConnectedPlayer> players = new ArrayList<ConnectedPlayer>();
    if(forClan) {
      List<UserClan> clanMembers = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId);
      for(UserClan uc : clanMembers) {
        ConnectedPlayer cp = playersByPlayerId.get(uc.getUserId());
        if(cp != null) {
          players.add(cp);
        }
      }
    }else {
      players = playersByPlayerId.values();
    }
    for(ConnectedPlayer player : players) {
      log.info("Sending chat message to player: "+player.getPlayerId());
      ReceivedGroupChatResponseEvent ce = new ReceivedGroupChatResponseEvent(player.getPlayerId());
      ce.setReceivedGroupChatResponseProto(chatProto.build());
      ce.setTag(tag);
      try {
        server.writeEvent(ce);
      }catch(Exception e) {
        log.error(e);
      }
    }
  }


  private void writeChangesToDB(User user) {
    if (!user.updateRelativeNumGroupChatsRemainingAndDiamonds(-1, 0)) {
      log.error("problem with decrementing a global chat");
    }
  }

  private boolean checkLegitSend(Builder resBuilder, User user,
      GroupChatScope scope, String chatMessage) {
    if (user == null || scope == null || chatMessage == null
        || chatMessage.length() == 0) {
      resBuilder.setStatus(SendGroupChatStatus.OTHER_FAIL);
      log.error("user is " + user + ", scope is " + scope
          + ", chatMessage=" + chatMessage);
      return false;
    }

    boolean isAlliance = MiscMethods.checkIfGoodSide(user.getType());
    if ((scope == GroupChatScope.ALLIANCE && !isAlliance)
        || (scope == GroupChatScope.LEGION && isAlliance)) {
      resBuilder.setStatus(SendGroupChatStatus.WRONG_SIDE);
      log.error("user type is " + user.getType() + ", scope is " + scope);
      return false;
    }

//    if (user.getNumGroupChatsRemaining() <= 0) {
//      resBuilder.setStatus(SendGroupChatStatus.NOT_ENOUGH_GROUP_CHATS);
//      log.error("user has no group chats remaining");
//      return false;
//    }

    if (chatMessage.length() > ControllerConstants.SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING) {
      resBuilder.setStatus(SendGroupChatStatus.TOO_LONG);
      log.error("chat message is too long. allowed is "
          + ControllerConstants.SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING
          + ", length is " + chatMessage.length()
          + ", chatMessage is " + chatMessage);
      return false;
    }

    resBuilder.setStatus(SendGroupChatStatus.SUCCESS);
    return true;
  }
}
