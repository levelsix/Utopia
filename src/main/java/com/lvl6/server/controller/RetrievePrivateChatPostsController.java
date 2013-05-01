package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrievePrivateChatPostsRequestEvent;
import com.lvl6.events.response.RetrievePrivateChatPostsResponseEvent;
import com.lvl6.info.PrivateChatPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrievePrivateChatPostsRequestProto;
import com.lvl6.proto.EventProto.RetrievePrivateChatPostsResponseProto;
import com.lvl6.proto.EventProto.RetrievePrivateChatPostsResponseProto.RetrievePrivateChatPostsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.PrivateChatPostRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrievePrivateChatPostsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrievePrivateChatPostsController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrievePrivateChatPostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_PRIVATE_CHAT_POST_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrievePrivateChatPostsRequestProto reqProto = ((RetrievePrivateChatPostsRequestEvent)event).getRetrievePrivateChatPostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int otherUserId = reqProto.getOtherUserId();
    int beforePrivateChatId = reqProto.getBeforePrivateChatId();

    RetrievePrivateChatPostsResponseProto.Builder resBuilder = RetrievePrivateChatPostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    if (reqProto.hasBeforePrivateChatId()) {
      resBuilder.setBeforePrivateChatId(beforePrivateChatId);
    }

    try {
      resBuilder.setStatus(RetrievePrivateChatPostsStatus.SUCCESS);

      List <PrivateChatPost> recentPrivateChatPosts;
      if (beforePrivateChatId > 0) {
        recentPrivateChatPosts = PrivateChatPostRetrieveUtils.getPrivateChatPostsBetweenUsersBeforePostId(
            ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, beforePrivateChatId, userId, otherUserId);        
      } else {
        recentPrivateChatPosts = PrivateChatPostRetrieveUtils.getPrivateChatPostsBetweenUsersBeforePostId(
            ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP,
            ControllerConstants.NOT_SET, userId, otherUserId);
      }
      if (recentPrivateChatPosts != null) {
        if (recentPrivateChatPosts != null && recentPrivateChatPosts.size() > 0) {
          List <Integer> userIds = new ArrayList<Integer>();
          userIds.add(userId);
          userIds.add(otherUserId);
          Map<Integer, User> usersByIds = null;
          if (userIds.size() > 0) {
            usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
            
            for (PrivateChatPost pwp : recentPrivateChatPosts) {
              resBuilder.addPosts(CreateInfoProtoUtils.createPrivateChatPostProtoFromPrivateChatPost(
                  pwp, usersByIds.get(pwp.getPosterId()), usersByIds.get(pwp.getRecipientId())));
            }
          }
        }
      } else {
        log.info("No private chat posts found for userId=" + userId + " and otherUserId=" + otherUserId); 
      }

      RetrievePrivateChatPostsResponseProto resProto = resBuilder.build();

      RetrievePrivateChatPostsResponseEvent resEvent = new RetrievePrivateChatPostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrievePrivateChatPostsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrievePrivateChatPostsController processEvent", e);
    }

  }

}