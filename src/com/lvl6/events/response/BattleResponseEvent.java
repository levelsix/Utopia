package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class BattleResponseEvent extends BroadcastResponseEvent {

  private BattleResponseProto battleResponseProto;
  
  public BattleResponseEvent(){
    eventType = EventProtocolResponse.S_BATTLE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = battleResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setBattleResponseProto(BattleResponseProto battleResponseProto) {
    this.battleResponseProto = battleResponseProto;
  }

}
