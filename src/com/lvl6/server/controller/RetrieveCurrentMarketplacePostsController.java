package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrentMarketplacePostsRequestEvent;
import com.lvl6.events.response.RetrieveCurrentMarketplacePostsResponseEvent;
import com.lvl6.info.MarketplacePost;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MarketplacePostRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class RetrieveCurrentMarketplacePostsController extends EventController{

  private static final int NUM_POSTS_CAP = ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP;
  
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

    List <MarketplacePost> activeMarketplacePosts;
    if (beforeThisPostId > 0) {
      if (forSender) {
        activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostIdForPoster(NUM_POSTS_CAP, beforeThisPostId, senderProto.getUserId());
      } else {
        activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsBeforePostId(NUM_POSTS_CAP, beforeThisPostId);        
      }
    } else {
      if (forSender) {
        activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePostsForPoster(NUM_POSTS_CAP, senderProto.getUserId());
      } else {
        activeMarketplacePosts = MarketplacePostRetrieveUtils.getMostRecentActiveMarketplacePosts(NUM_POSTS_CAP);
      }
    }
    if (activeMarketplacePosts != null) {
      for (MarketplacePost mp : activeMarketplacePosts) {
        resBuilder.addMarketplacePosts(CreateInfoProtoUtils.createFullMarketplacePostProtoFromMarketplacePost(mp));
      }
    }
    RetrieveCurrentMarketplacePostsResponseProto resProto = resBuilder.build();

    RetrieveCurrentMarketplacePostsResponseEvent resEvent = new RetrieveCurrentMarketplacePostsResponseEvent(senderProto.getUserId());
    resEvent.setRetrieveCurrentMarketplacePostsResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
