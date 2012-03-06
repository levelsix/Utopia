package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrentMarketplacePostsRequestEvent;
import com.lvl6.events.response.RetrieveCurrentMarketplacePostsResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto.RetrieveCurrentMarketplacePostsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class RetrieveCurrentMarketplacePostsController extends EventController{

  public RetrieveCurrentMarketplacePostsController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveCurrentMarketplacePostsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveCurrentMarketplacePostsRequestProto reqProto = ((RetrieveCurrentMarketplacePostsRequestEvent)event).getRetrieveCurrentMarketplacePostsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int beforeThisPostId = reqProto.getBeforeThisPostId();
    boolean forSender = reqProto.getFromSender();

    RetrieveCurrentMarketplacePostsResponseProto.Builder resBuilder = RetrieveCurrentMarketplacePostsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setFromSender(forSender);
    if (beforeThisPostId > 0) {
      resBuilder.setBeforeThisPostId(beforeThisPostId);
    }

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      if (user == null) {
        resBuilder.setStatus(RetrieveCurrentMarketplacePostsStatus.OTHER_FAIL);
      } else if (user.getLevel() < ControllerConstants.MIN_LEVEL_FOR_MARKETPLACE) {
        resBuilder.setStatus(RetrieveCurrentMarketplacePostsStatus.LEVEL_TOO_LOW);
      } else {
        resBuilder.setStatus(RetrieveCurrentMarketplacePostsStatus.SUCCESS);
        List <MarketplacePost> activeMarketplacePosts;
        if (beforeThisPostId > 0) {
          resBuilder.setBeforeThisPostId(beforeThisPostId);
          if (forSender) {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostIdForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, beforeThisPostId, senderProto.getUserId());
          } else {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostId(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, beforeThisPostId);        
          }
        } else {
          if (forSender) {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsForPoster(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP, senderProto.getUserId());
          } else {
            activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP);
          }
        }
        if (activeMarketplacePosts != null) {
          for (MarketplacePost mp : activeMarketplacePosts) {
            resBuilder.addMarketplacePosts(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp));
          }
        }
      }
      RetrieveCurrentMarketplacePostsResponseProto resProto = resBuilder.build();

      RetrieveCurrentMarketplacePostsResponseEvent resEvent = new RetrieveCurrentMarketplacePostsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrentMarketplacePostsResponseProto(resProto);

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveCurrentMarketplacePostsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }

  }

}
