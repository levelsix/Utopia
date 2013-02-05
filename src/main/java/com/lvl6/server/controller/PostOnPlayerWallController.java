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
import com.lvl6.events.request.PostOnPlayerWallRequestEvent;
import com.lvl6.events.response.PostOnPlayerWallResponseEvent;
import com.lvl6.info.PlayerWallPost;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PostOnPlayerWallRequestProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto.Builder;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto.PostOnPlayerWallStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PlayerWallPostProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.BannedUserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PostOnPlayerWallController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  
  public PostOnPlayerWallController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PostOnPlayerWallRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_POST_ON_PLAYER_WALL_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
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
    Map<Integer, User> users = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
    boolean legitPost = checkLegitPost(resBuilder, posterId, wallOwnerId, content, users);

    PostOnPlayerWallResponseEvent resEvent = new PostOnPlayerWallResponseEvent(posterId);
    resEvent.setTag(event.getTag());

    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      String censoredContent = MiscMethods.censorUserInput(content);
      int wallPostId = insertUtils.insertPlayerWallPost(posterId, wallOwnerId, censoredContent, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PostOnPlayerWallStatus.OTHER_FAIL);
        log.error("problem with inserting wall post into db. posterId=" + posterId + ", wallOwnerId="
            + wallOwnerId + ", content=" + content +  ", censoredContent=" + censoredContent 
            + ", timeOfPost=" + timeOfPost);
      } else {
        PlayerWallPost pwp =  new PlayerWallPost(wallPostId, posterId, wallOwnerId, timeOfPost, censoredContent);
        PlayerWallPostProto pwpp = CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(pwp, users.get(posterId));
        resBuilder.setPost(pwpp);

        PostOnPlayerWallResponseEvent resEvent2 = new PostOnPlayerWallResponseEvent(wallOwnerId);
        resEvent2.setPostOnPlayerWallResponseProto(resBuilder.build());
        server.writeAPNSNotificationOrEvent(resEvent2);
      }
    }
    resEvent.setPostOnPlayerWallResponseProto(resBuilder.build());
    server.writeEvent(resEvent);

    if (legitPost && wallOwnerId != posterId) {
      User wallOwner = users.get(wallOwnerId);
      User poster = users.get(posterId);
      if (MiscMethods.checkIfGoodSide(wallOwner.getType()) == !MiscMethods.checkIfGoodSide(poster.getType())) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, posterId, senderProto, SpecialQuestAction.WRITE_ON_ENEMY_WALL, true);
      }
    }

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
    Set<Integer> banned = BannedUserRetrieveUtils.getAllBannedUsers();
    if(null != banned && banned.contains(posterId)) {
      resBuilder.setStatus(PostOnPlayerWallStatus.BANNED);
      log.warn("banned user tried to send a post. posterId=" + posterId);
      return false;
    }
    resBuilder.setStatus(PostOnPlayerWallStatus.SUCCESS);
    return true;
  }
}
