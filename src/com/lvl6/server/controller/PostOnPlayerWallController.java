package com.lvl6.server.controller;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostOnPlayerWallRequestEvent;
import com.lvl6.events.response.PostOnPlayerWallResponseEvent;
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
import com.lvl6.utils.utilmethods.InsertUtils;

public class PostOnPlayerWallController extends EventController {

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

    boolean legitPost = checkLegitPost(resBuilder, posterId, wallOwnerId, content);

    PostOnPlayerWallResponseEvent resEvent = new PostOnPlayerWallResponseEvent(posterId);
    resEvent.setTag(event.getTag());
    
    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      int wallPostId = InsertUtils.insertPlayerWallPost(posterId, wallOwnerId, content, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      } else {
        PlayerWallPostProto pwp = PlayerWallPostProto.newBuilder().setId(wallPostId)
            .setPosterId(posterId).setWallOwnerId(wallOwnerId).setTimeOfPost(timeOfPost.getTime())
            .setContent(content).build();
        resBuilder.setPost(pwp);

        PostOnPlayerWallResponseEvent resEvent2 = new PostOnPlayerWallResponseEvent(posterId);
        resEvent2.setPostOnPlayerWallResponseProto(resBuilder.build());
        server.writeAPNSNotificationOrEvent(resEvent2);
      }
    }
    resEvent.setPostOnPlayerWallResponseProto(resBuilder.build());
    server.writeEvent(resEvent);
  }


  private boolean checkLegitPost(Builder resBuilder, int posterId, int wallOwnerId, String content) {
    // TODO Auto-generated method stub
    List<Integer> userIds = new ArrayList<Integer>();
    userIds.add(posterId);
    userIds.add(wallOwnerId);
    Map<Integer, User> users = UserRetrieveUtils.getUsersByIds(userIds);
    if (users.size() != 2 && posterId != wallOwnerId) {
      resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      return false;
    }
    if (users.size() != 1 && posterId == wallOwnerId) {
      resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
      return false;
    }
    if (content == null || content.length() == 0) {
      resBuilder.setStatus(PostOnPlayerWallStatus.NO_CONTENT_SENT);
      return false;
    }
    if (content.length() >= ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(PostOnPlayerWallStatus.POST_TOO_LARGE);
      return false;
    }
    resBuilder.setStatus(PostOnPlayerWallStatus.SUCCESS);
    return true;
  }
}
