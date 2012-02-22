package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PostToMarketplaceResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PostToMarketplaceResponseEvent extends NormalResponseEvent {

  private PostToMarketplaceResponseProto postToMarketplaceResponseProto;
  
  public PostToMarketplaceResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_POST_TO_MARKETPLACE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = postToMarketplaceResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPostToMarketplaceResponseProto(PostToMarketplaceResponseProto postToMarketplaceResponseProto) {
    this.postToMarketplaceResponseProto = postToMarketplaceResponseProto;
  }

}
