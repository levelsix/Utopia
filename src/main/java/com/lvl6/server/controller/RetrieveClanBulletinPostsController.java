package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.RetrieveClanBulletinPostsRequestEvent;
import com.lvl6.events.response.RetrieveClanBulletinPostsResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanBulletinPost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveClanBulletinPostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveClanBulletinPostsResponseProto;
import com.lvl6.proto.EventProto.RetrieveClanBulletinPostsResponseProto.RetrieveClanBulletinPostsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanBulletinPostRetrieveUtils;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrieveClanBulletinPostsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveClanBulletinPostsController() {
    numAllocatedThreads = 5;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveClanBulletinPostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CLAN_BULLETIN_POSTS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveClanBulletinPostsRequestProto reqProto = ((RetrieveClanBulletinPostsRequestEvent)event).getRetrieveClanBulletinPostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int beforeThisPostId = reqProto.getBeforeThisPostId();

    RetrieveClanBulletinPostsResponseProto.Builder resBuilder = RetrieveClanBulletinPostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    if (reqProto.hasBeforeThisPostId()) resBuilder.setBeforeThisPostId(beforeThisPostId);

    try {
      Clan clan = null;
      if (reqProto.getSender().hasClan() && reqProto.getSender().getClan().hasClanId()) {
        clan = ClanRetrieveUtils.getClanWithId(reqProto.getSender().getClan().getClanId());
      }

      if (clan == null) {
        resBuilder.setStatus(RetrieveClanBulletinPostsStatus.NOT_IN_CLAN);
        log.error("USER not in clan.");
      } else {
        resBuilder.setStatus(RetrieveClanBulletinPostsStatus.SUCCESS);

        List <ClanBulletinPost> activeClanBulletinPosts;
        if (beforeThisPostId > 0) {
          activeClanBulletinPosts = ClanBulletinPostRetrieveUtils.getMostRecentActiveClanBulletinPostsForClanBeforePostId(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, beforeThisPostId, clan.getId());        
        } else {
          activeClanBulletinPosts = ClanBulletinPostRetrieveUtils.getMostRecentClanBulletinPostsForClan(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, clan.getId());
        }
        if (activeClanBulletinPosts != null) {
          if (activeClanBulletinPosts != null && activeClanBulletinPosts.size() > 0) {
            List <Integer> userIds = new ArrayList<Integer>();
            for (ClanBulletinPost p : activeClanBulletinPosts) {
              userIds.add(p.getPosterId());
            }
            Map<Integer, User> usersByIds = null;
            if (userIds.size() > 0) {
              usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
              for (ClanBulletinPost pwp : activeClanBulletinPosts) {
                resBuilder.addClanBulletinPosts(CreateInfoProtoUtils.createClanBulletinPostProtoFromClanBulletinPost(pwp, usersByIds.get(pwp.getPosterId())));
              }
            }
          }
        }
      }
      RetrieveClanBulletinPostsResponseProto resProto = resBuilder.build();

      RetrieveClanBulletinPostsResponseEvent resEvent = new RetrieveClanBulletinPostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveClanBulletinPostsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveClanBulletinPostsController processEvent", e);
    }

  }

}