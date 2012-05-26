package com.lvl6.server.controller;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostOnPlayerWallRequestEvent;
import com.lvl6.events.response.PostOnPlayerWallResponseEvent;
import com.lvl6.info.PlayerWallPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PostOnPlayerWallRequestProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto.Builder;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto.PostOnPlayerWallStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PlayerWallPostProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

public class PostOnPlayerWallController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public PostOnPlayerWallController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PostOnPlayerWallRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_POST_ON_PLAYER_WALL;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    PostOnPlayerWallRequestProto reqProto = ((PostOnPlayerWallRequestEvent)event).getPostOnPlayerWallRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int posterId = senderProto.getUserId();
    int wallOwnerId = reqProto.getWallOwnerId();
    String content = (reqProto.hasContent()) ? reqProto.getContent() : "";

    PostOnPlayerWallResponseProto.Builder resBuilder = PostOnPlayerWallResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    List<Integer> userIds = new ArrayList<Integer>();
    userIds.add(posterId);
    userIds.add(wallOwnerId);
    Map<Integer, User> users = UserRetrieveUtils.getUsersByIds(userIds);
    boolean legitPost = checkLegitPost(resBuilder, posterId, wallOwnerId, content, users);

    PostOnPlayerWallResponseEvent resEvent = new PostOnPlayerWallResponseEvent(posterId);
    resEvent.setTag(event.getTag());
    
    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      int wallPostId = InsertUtils.insertPlayerWallPost(posterId, wallOwnerId, content, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
        log.error("problem with inserting wall post into db. posterId=" + posterId + ", wallOwnerId="
            + wallOwnerId + ", content=" + content + ", timeOfPost=" + timeOfPost);
      } else {
        PlayerWallPost pwp =  new PlayerWallPost(wallPostId, posterId, wallOwnerId, timeOfPost, content);
        PlayerWallPostProto pwpp = CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(pwp, users.get(posterId));
        resBuilder.setPost(pwpp);

        PostOnPlayerWallResponseEvent resEvent2 = new PostOnPlayerWallResponseEvent(posterId);
        resEvent2.setPostOnPlayerWallResponseProto(resBuilder.build());
        server.writeAPNSNotificationOrEvent(resEvent2);
      }
    }
    resEvent.setPostOnPlayerWallResponseProto(resBuilder.build());
    server.writeEvent(resEvent);
  }


  private boolean checkLegitPost(Builder resBuilder, int posterId, int wallOwnerId, String content, Map<Integer, User> users) {
    // TODO Auto-generated method stub
    if (users == null) {
      resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      log.error("users are null- posterId=" + posterId + ", wallOwnerId=" + wallOwnerId);
      return false;
    }
    if (users.size() != 2 && posterId != wallOwnerId) {
      resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      log.error("error retrieving one of the users. posterId=" + posterId + ", wallOwnerId=" + wallOwnerId);
      return false;
    }
    if (users.size() != 1 && posterId == wallOwnerId) {
      resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      log.error("error retrieving one of the users. posterId=" + posterId + ", wallOwnerId=" + wallOwnerId);
      return false;
    }
    if (content == null || content.length() == 0) {
      resBuilder.setStatus(PostOnPlayerWallStatus.NO_CONTENT_SENT);
      log.error("no content when posterId " + posterId + " tries to post on wall with owner " + wallOwnerId);
      return false;
    }
    if (content.length() >= ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(PostOnPlayerWallStatus.POST_TOO_LARGE);
      log.error("wall post is too long. content length is " + content.length() 
          +", max post length=" + ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH 
          + ", posterId " + posterId + " tries to post on wall with owner " + wallOwnerId);
      return false;
    }
    resBuilder.setStatus(PostOnPlayerWallStatus.SUCCESS);
    return true;
  }
}
