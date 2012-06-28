package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PostOnPlayerWallResponseEvent extends NormalResponseEvent {

  private PostOnPlayerWallResponseProto postOnPlayerWallResponseProto;
  
  public PostOnPlayerWallResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_POST_ON_PLAYER_WALL_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = postOnPlayerWallResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPostOnPlayerWallResponseProto(PostOnPlayerWallResponseProto postOnPlayerWallResponseProto) {
    this.postOnPlayerWallResponseProto = postOnPlayerWallResponseProto;
  }

  public PostOnPlayerWallResponseProto getPostOnPlayerWallResponseProto() { //required for APNS
    return postOnPlayerWallResponseProto;
  }
  
}
