package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PostOnClanWallResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PostOnClanWallResponseEvent extends NormalResponseEvent {

  private PostOnClanWallResponseProto postOnClanWallResponseProto;
  
  public PostOnClanWallResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_POST_ON_CLAN_WALL_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = postOnClanWallResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPostOnClanWallResponseProto(PostOnClanWallResponseProto postOnClanWallResponseProto) {
    this.postOnClanWallResponseProto = postOnClanWallResponseProto;
  }

  public PostOnClanWallResponseProto getPostOnClanWallResponseProto() { //required for APNS
    return postOnClanWallResponseProto;
  }
  
}
