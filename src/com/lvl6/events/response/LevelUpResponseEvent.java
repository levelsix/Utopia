package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.LevelUpResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class LevelUpResponseEvent extends NonBroadcastResponseEvent {

  private LevelUpResponseProto levelUpResponseProto;
  
  public LevelUpResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_LEVEL_UP_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = levelUpResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setLevelUpResponseProto(LevelUpResponseProto levelUpResponseProto) {
    this.levelUpResponseProto = levelUpResponseProto;
  }

}
