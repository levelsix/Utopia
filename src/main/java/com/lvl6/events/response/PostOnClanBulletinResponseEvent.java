package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PostOnClanBulletinResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PostOnClanBulletinResponseEvent extends NormalResponseEvent {

  private PostOnClanBulletinResponseProto postOnClanBulletinResponseProto;
  
  public PostOnClanBulletinResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_POST_ON_CLAN_BULLETIN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = postOnClanBulletinResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPostOnClanBulletinResponseProto(PostOnClanBulletinResponseProto postOnClanBulletinResponseProto) {
    this.postOnClanBulletinResponseProto = postOnClanBulletinResponseProto;
  }

  public PostOnClanBulletinResponseProto getPostOnClanBulletinResponseProto() { //required for APNS
    return postOnClanBulletinResponseProto;
  }
  
}
