package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveClanWallPostsRequestEvent;
import com.lvl6.events.response.RetrieveClanWallPostsResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanWallPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveClanWallPostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveClanWallPostsResponseProto;
import com.lvl6.proto.EventProto.RetrieveClanWallPostsResponseProto.RetrieveClanWallPostsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanWallPostRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrieveClanWallPostsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveClanWallPostsController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveClanWallPostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CLAN_WALL_POSTS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveClanWallPostsRequestProto reqProto = ((RetrieveClanWallPostsRequestEvent)event).getRetrieveClanWallPostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int beforeThisPostId = reqProto.getBeforeThisPostId();

    RetrieveClanWallPostsResponseProto.Builder resBuilder = RetrieveClanWallPostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    if (reqProto.hasBeforeThisPostId()) resBuilder.setBeforeThisPostId(beforeThisPostId);

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      if (user == null) {
        resBuilder.setStatus(RetrieveClanWallPostsStatus.OTHER_FAIL);
        log.error("no user with id " + senderProto.getUserId());
      } else {
        Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());

        if (clan == null) {
          resBuilder.setStatus(RetrieveClanWallPostsStatus.NOT_IN_CLAN);
          log.error("USER not in clan. user: " + user);
        } else {
          resBuilder.setStatus(RetrieveClanWallPostsStatus.SUCCESS);

          List <ClanWallPost> activeClanWallPosts;
          if (beforeThisPostId > 0) {
            activeClanWallPosts = ClanWallPostRetrieveUtils.getMostRecentActiveClanWallPostsForClanBeforePostId(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, beforeThisPostId, clan.getId());        
          } else {
            activeClanWallPosts = ClanWallPostRetrieveUtils.getMostRecentClanWallPostsForClan(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, clan.getId());
          }
          if (activeClanWallPosts != null) {
            if (activeClanWallPosts != null && activeClanWallPosts.size() > 0) {
              List <Integer> userIds = new ArrayList<Integer>();
              for (ClanWallPost p : activeClanWallPosts) {
                userIds.add(p.getPosterId());
              }
              Map<Integer, User> usersByIds = null;
              if (userIds.size() > 0) {
                usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
                for (ClanWallPost pwp : activeClanWallPosts) {
                  resBuilder.addClanWallPosts(CreateInfoProtoUtils.createClanWallPostProtoFromClanWallPost(pwp, usersByIds.get(pwp.getPosterId())));
                }
              }
            }
          }
        }
      }
      RetrieveClanWallPostsResponseProto resProto = resBuilder.build();

      RetrieveClanWallPostsResponseEvent resEvent = new RetrieveClanWallPostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveClanWallPostsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveClanWallPostsController processEvent", e);
    } finally {
    }

  }

}