package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostOnClanWallRequestEvent;
import com.lvl6.events.response.PostOnClanWallResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanWallPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PostOnClanWallRequestProto;
import com.lvl6.proto.EventProto.PostOnClanWallResponseProto;
import com.lvl6.proto.EventProto.PostOnClanWallResponseProto.Builder;
import com.lvl6.proto.EventProto.PostOnClanWallResponseProto.PostOnClanWallStatus;
import com.lvl6.proto.InfoProto.ClanWallPostProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class PostOnClanWallController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
  this.insertUtils = insertUtils;
  }

  
  public PostOnClanWallController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PostOnClanWallRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_POST_ON_CLAN_WALL_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PostOnClanWallRequestProto reqProto = ((PostOnClanWallRequestEvent)event).getPostOnClanWallRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String content = (reqProto.hasContent()) ? reqProto.getContent() : "";

    PostOnClanWallResponseProto.Builder resBuilder = PostOnClanWallResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
    Clan clan = (user == null) ? null : ClanRetrieveUtils.getClanWithId(user.getClanId());
    
    boolean legitPost = checkLegitPost(resBuilder, user, clan, content);

    PostOnClanWallResponseEvent resEvent = new PostOnClanWallResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());

    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      int wallPostId = insertUtils.insertClanWallPost(user.getId(), clan.getId(), content, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PostOnClanWallStatus.OTHER_FAIL);
        log.error("problem with inserting clan wall post into db. posterId=" + user.getId() + ", clanid="
            + clan.getId() + ", content=" + content + ", timeOfPost=" + timeOfPost);
      } else {
        ClanWallPost cwp =  new ClanWallPost(wallPostId, user.getId(), clan.getId(), timeOfPost, content);
        ClanWallPostProto cwpp = CreateInfoProtoUtils.createClanWallPostProtoFromClanWallPost(cwp, user);
        resBuilder.setPost(cwpp);
      }
    }
    resEvent.setPostOnClanWallResponseProto(resBuilder.build());
    server.writeEvent(resEvent);
  }


  private boolean checkLegitPost(Builder resBuilder, User user, Clan clan,
      String content) {
    if (user == null) {
      resBuilder.setStatus(PostOnClanWallStatus.OTHER_FAIL);
      log.error("users is null");
      return false;
    }
    if (clan == null) {
      resBuilder.setStatus(PostOnClanWallStatus.NOT_IN_CLAN);
      log.error("user not in clan. user is " + user);
      return false;
    }
    if (content == null || content.length() == 0) {
      resBuilder.setStatus(PostOnClanWallStatus.NO_CONTENT_SENT);
      log.error("no content when user is " + user + " tries to post on clan with id " + clan.getId());
      return false;
    }
    if (content.length() >= ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(PostOnClanWallStatus.POST_TOO_LARGE);
      log.error("wall post is too long. content length is " + content.length() 
          +", max post length=" + ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH); 
      return false;
    }
    resBuilder.setStatus(PostOnClanWallStatus.SUCCESS);
    return true;
  }

}
