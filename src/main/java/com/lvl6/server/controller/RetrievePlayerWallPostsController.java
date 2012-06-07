package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrievePlayerWallPostsRequestEvent;
import com.lvl6.events.response.RetrievePlayerWallPostsResponseEvent;
import com.lvl6.info.PlayerWallPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrievePlayerWallPostsRequestProto;
import com.lvl6.proto.EventProto.RetrievePlayerWallPostsResponseProto;
import com.lvl6.proto.EventProto.RetrievePlayerWallPostsResponseProto.RetrievePlayerWallPostsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.PlayerWallPostRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

  @Component @DependsOn("gameServer") public class RetrievePlayerWallPostsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrievePlayerWallPostsController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrievePlayerWallPostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_PLAYER_WALL_POSTS;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrievePlayerWallPostsRequestProto reqProto = ((RetrievePlayerWallPostsRequestEvent)event).getRetrievePlayerWallPostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int relevantUserId = reqProto.getRelevantUserId();
    int beforeThisPostId = reqProto.getBeforeThisPostId();

    RetrievePlayerWallPostsResponseProto.Builder resBuilder = RetrievePlayerWallPostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setRelevantUserId(relevantUserId);
    if (reqProto.hasBeforeThisPostId()) resBuilder.setBeforeThisPostId(beforeThisPostId);

    server.lockPlayer(relevantUserId);
    try {
      User user = UserRetrieveUtils.getUserById(relevantUserId);
      if (user == null) {
        resBuilder.setStatus(RetrievePlayerWallPostsStatus.OTHER_FAIL);
        log.error("no user with id " + relevantUserId);
      } else {
        resBuilder.setStatus(RetrievePlayerWallPostsStatus.SUCCESS);
        
        List <PlayerWallPost> activePlayerWallPosts;
        if (beforeThisPostId > 0) {
          activePlayerWallPosts = PlayerWallPostRetrieveUtils.getMostRecentActivePlayerWallPostsForPlayerBeforePostId(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, beforeThisPostId, relevantUserId);        
        } else {
          activePlayerWallPosts = PlayerWallPostRetrieveUtils.getMostRecentPlayerWallPostsForWallOwner(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, relevantUserId);
        }
        if (activePlayerWallPosts != null) {
          if (activePlayerWallPosts != null && activePlayerWallPosts.size() > 0) {
            List <Integer> userIds = new ArrayList<Integer>();
            for (PlayerWallPost p : activePlayerWallPosts) {
              userIds.add(p.getPosterId());
            }
            Map<Integer, User> usersByIds = null;
            if (userIds.size() > 0) {
              usersByIds = UserRetrieveUtils.getUsersByIds(userIds);
              for (PlayerWallPost pwp : activePlayerWallPosts) {
                resBuilder.addPlayerWallPosts(CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(pwp, usersByIds.get(pwp.getPosterId())));
              }
            }
          }
        }
      }
      RetrievePlayerWallPostsResponseProto resProto = resBuilder.build();

      RetrievePlayerWallPostsResponseEvent resEvent = new RetrievePlayerWallPostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrievePlayerWallPostsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrievePlayerWallPostsController processEvent", e);
    } finally {
      server.unlockPlayer(relevantUserId); 
    }

  }

}