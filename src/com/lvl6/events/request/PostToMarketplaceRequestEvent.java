package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PostToMarketplaceRequestProto;

public class PostToMarketplaceRequestEvent extends RequestEvent {

  private PostToMarketplaceRequestProto postToMarketplaceRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      postToMarketplaceRequestProto = PostToMarketplaceRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = postToMarketplaceRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PostToMarketplaceRequestProto getPostToMarketplaceRequestProto() {
    return postToMarketplaceRequestProto;
  }
}
