package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PostOnClanBulletinRequestEvent;
import com.lvl6.events.response.PostOnClanBulletinResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanBulletinPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.PostOnClanBulletinRequestProto;
import com.lvl6.proto.EventProto.PostOnClanBulletinResponseProto;
import com.lvl6.proto.EventProto.PostOnClanBulletinResponseProto.Builder;
import com.lvl6.proto.EventProto.PostOnClanBulletinResponseProto.PostOnClanBulletinStatus;
import com.lvl6.proto.InfoProto.ClanBulletinPostProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;

  @Component @DependsOn("gameServer") public class PostOnClanBulletinController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
  this.insertUtils = insertUtils;
  }

  
  public PostOnClanBulletinController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new PostOnClanBulletinRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_POST_ON_CLAN_BULLETIN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PostOnClanBulletinRequestProto reqProto = ((PostOnClanBulletinRequestEvent)event).getPostOnClanBulletinRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String content = (reqProto.hasContent()) ? reqProto.getContent() : "";

    PostOnClanBulletinResponseProto.Builder resBuilder = PostOnClanBulletinResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
    Clan clan = (user == null) ? null : ClanRetrieveUtils.getClanWithId(user.getClanId());
    
    boolean legitPost = checkLegitPost(resBuilder, user, clan, content);

    PostOnClanBulletinResponseEvent resEvent = new PostOnClanBulletinResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());

    if (legitPost) {
      Timestamp timeOfPost = new Timestamp(new Date().getTime());
      int wallPostId = insertUtils.insertClanBulletinPost(user.getId(), clan.getId(), content, timeOfPost);
      if (wallPostId <= 0) {
        legitPost = false;
        resBuilder.setStatus(PostOnClanBulletinStatus.OTHER_FAIL);
        log.error("problem with inserting clan wall post into db. posterId=" + user.getId() + ", clanid="
            + clan.getId() + ", content=" + content + ", timeOfPost=" + timeOfPost);
      } else {
        ClanBulletinPost cwp =  new ClanBulletinPost(wallPostId, user.getId(), clan.getId(), timeOfPost, content);
        ClanBulletinPostProto cwpp = CreateInfoProtoUtils.createClanBulletinPostProtoFromClanBulletinPost(cwp, user);
        resBuilder.setPost(cwpp);
      }
      resEvent.setPostOnClanBulletinResponseProto(resBuilder.build());
      server.writeClanEvent(resEvent, clan.getId());
    }else {
	    resEvent.setPostOnClanBulletinResponseProto(resBuilder.build());
	    server.writeEvent(resEvent);
    }
  }


  private boolean checkLegitPost(Builder resBuilder, User user, Clan clan,
      String content) {
    if (user == null) {
      resBuilder.setStatus(PostOnClanBulletinStatus.OTHER_FAIL);
      log.error("users is null");
      return false;
    }
    if (clan == null) {
      resBuilder.setStatus(PostOnClanBulletinStatus.NOT_IN_CLAN);
      log.error("user not in clan. user is " + user);
      return false;
    }
    if (user.getId() != clan.getOwnerId()) {
      resBuilder.setStatus(PostOnClanBulletinStatus.OTHER_FAIL);
      log.error("user not leader. user is " + user + ". clan is "+clan);
      return false;
    }
    if (content == null || content.length() == 0) {
      resBuilder.setStatus(PostOnClanBulletinStatus.NO_CONTENT_SENT);
      log.error("no content when user is " + user + " tries to post on clan with id " + clan.getId());
      return false;
    }
    if (content.length() >= ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(PostOnClanBulletinStatus.POST_TOO_LARGE);
      log.error("wall post is too long. content length is " + content.length() 
          +", max post length=" + ControllerConstants.POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH); 
      return false;
    }
    resBuilder.setStatus(PostOnClanBulletinStatus.SUCCESS);
    return true;
  }

}
