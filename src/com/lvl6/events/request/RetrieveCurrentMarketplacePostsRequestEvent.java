package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsRequestProto;

public class RetrieveCurrentMarketplacePostsRequestEvent extends RequestEvent{
  private RetrieveCurrentMarketplacePostsRequestProto retrieveCurrentMarketplacePostsRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveCurrentMarketplacePostsRequestProto = RetrieveCurrentMarketplacePostsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveCurrentMarketplacePostsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveCurrentMarketplacePostsRequestProto getRetrieveCurrentMarketplacePostsRequestProto() {
    return retrieveCurrentMarketplacePostsRequestProto;
  }
  
}//RetrieveCurrentMarketplacePostsRequestProto