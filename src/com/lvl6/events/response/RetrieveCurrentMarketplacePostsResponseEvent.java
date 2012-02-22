package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveCurrentMarketplacePostsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveCurrentMarketplacePostsResponseEvent extends NormalResponseEvent {

  private RetrieveCurrentMarketplacePostsResponseProto retrieveCurrentMarketplacePostsResponseProto;
  
  public RetrieveCurrentMarketplacePostsResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = retrieveCurrentMarketplacePostsResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setRetrieveCurrentMarketplacePostsResponseProto(
      RetrieveCurrentMarketplacePostsResponseProto retrieveCurrentMarketplacePostsResponseProto) {
    this.retrieveCurrentMarketplacePostsResponseProto = retrieveCurrentMarketplacePostsResponseProto;
  }
  
}
