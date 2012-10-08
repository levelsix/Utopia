package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.BossActionResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class BossActionResponseEvent extends NormalResponseEvent {

  private BossActionResponseProto bossActionResponseProto;
  
  public BossActionResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_BOSS_ACTION_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = bossActionResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setBossActionResponseProto(BossActionResponseProto bossActionResponseProto) {
    this.bossActionResponseProto = bossActionResponseProto;
  }

}
