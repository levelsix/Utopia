package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PrivateChatPostRequestEvent;
import com.lvl6.events.response.PrivateChatPostResponseEvent;
import com.lvl6.info.PrivateChatPost;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PrivateChatPostRequestProto;
import com.lvl6.proto.EventProto.PrivateChatPostResponseProto;
import com.lvl6.proto.EventProto.PrivateChatPostResponseProto.Builder;
import com.lvl6.proto.EventProto.PrivateChatPostResponseProto.PrivateChatPostStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PrivateChatPostProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.BannedUserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PrivateChatPostController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  
  public PrivateChatPostController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PrivateChatPostRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PRIVATE_CHAT_POST_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PrivateChatPostRequestProto reqProto = ((PrivateChatPostRequestEvent)event).getPrivateChatPostRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int posterId = senderProto.getUserId();
    int recipientId = reqProto.getRecipientId();
    String content = (reqProto.hasContent()) ? reqProto.getContent() : "";

    PrivateChatPostResponseProto.Builder resBuilder = PrivateChatPostResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    List<Integer> userIds = new ArrayList<Integer>();
    userIds.add(posterId);
    userIds.add(recipientId);
    Map<Integer, User> users = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
    boolean legitPost = checkLegitPost(resBuilder, posterId, recipientId, content, users);

    PrivateChatPostResponseEvent resEvent = new PrivateChatPostResponseEvent(posterId);
    resEvent.setTag(event.getTag());

    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      String censoredContent = MiscMethods.censorUserInput(content);
      int wallPostId = insertUtils.insertIntoPrivatePosts(posterId, recipientId, censoredContent, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PrivateChatPostStatus.OTHER_FAIL);
        log.error("problem with inserting private chat post into db. posterId=" + posterId + ", recipientId="
            + recipientId + ", content=" + content +  ", censoredContent=" + censoredContent 
            + ", timeOfPost=" + timeOfPost);
      } else {
        PrivateChatPost pwp =  new PrivateChatPost(wallPostId, posterId, recipientId, timeOfPost, censoredContent);
        PrivateChatPostProto pcpp = CreateInfoProtoUtils.createPrivateChatPostProtoFromPrivateChatPost(pwp, users.get(posterId));
        resBuilder.setPost(pcpp);

        PrivateChatPostResponseEvent resEvent2 = new PrivateChatPostResponseEvent(recipientId);
        resEvent2.setPrivateChatPostResponseProto(resBuilder.build());
        server.writeAPNSNotificationOrEvent(resEvent2);
      }
    }
    resEvent.setPrivateChatPostResponseProto(resBuilder.build());
    server.writeEvent(resEvent);

//    if (legitPost && recipientId != posterId) {
//      User wallOwner = users.get(recipientId);
//      User poster = users.get(posterId);
//      if (MiscMethods.checkIfGoodSide(wallOwner.getType()) == !MiscMethods.checkIfGoodSide(poster.getType())) {
//        QuestUtils.checkAndSendQuestsCompleteBasic(server, posterId, senderProto, SpecialQuestAction.WRITE_ON_ENEMY_WALL, true);
//      }
//    }

  }


  private boolean checkLegitPost(Builder resBuilder, int posterId, int recipientId, String content, Map<Integer, User> users) {
    // TODO Auto-generated method stub
    if (users == null) {
      resBuilder.setStatus(PrivateChatPostStatus.OTHER_FAIL);
      log.error("users are null- posterId=" + posterId + ", recipientId=" + recipientId);
      return false;
    }
    if (users.size() != 2 && posterId != recipientId) {
      resBuilder.setStatus(PrivateChatPostStatus.OTHER_FAIL);
      log.error("error retrieving one of the users. posterId=" + posterId + ", recipientId=" + recipientId);
      return false;
    }
    if (users.size() != 1 && posterId == recipientId) {
      resBuilder.setStatus(PrivateChatPostStatus.OTHER_FAIL);
      log.error("error retrieving one of the users. posterId=" + posterId + ", recipientId=" + recipientId);
      return false;
    }
    if (content == null || content.length() == 0) {
      resBuilder.setStatus(PrivateChatPostStatus.NO_CONTENT_SENT);
      log.error("no content when posterId " + posterId + " tries to post on wall with owner " + recipientId);
      return false;
    }
    if (content.length() >= ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(PrivateChatPostStatus.POST_TOO_LARGE);
      log.error("wall post is too long. content length is " + content.length() 
          +", max post length=" + ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH 
          + ", posterId " + posterId + " tries to post on wall with owner " + recipientId);
      return false;
    }
    Set<Integer> banned = BannedUserRetrieveUtils.getAllBannedUsers();
    if(null != banned && banned.contains(posterId)) {
      resBuilder.setStatus(PrivateChatPostStatus.BANNED);
      log.warn("banned user tried to send a post. posterId=" + posterId);
      return false;
    }
    resBuilder.setStatus(PrivateChatPostStatus.SUCCESS);
    return true;
  }
}
