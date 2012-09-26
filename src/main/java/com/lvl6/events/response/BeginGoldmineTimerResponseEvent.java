package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.BeginGoldmineTimerResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class BeginGoldmineTimerResponseEvent extends NormalResponseEvent {

  private BeginGoldmineTimerResponseProto beginGoldmineTimerResponseProto;
  
  public BeginGoldmineTimerResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_BATTLE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = beginGoldmineTimerResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setBeginGoldmineTimerResponseProto(BeginGoldmineTimerResponseProto beginGoldmineTimerResponseProto) {
    this.beginGoldmineTimerResponseProto = beginGoldmineTimerResponseProto;
  }

  public BeginGoldmineTimerResponseProto getBeginGoldmineTimerResponseProto() {   //because APNS required
    return beginGoldmineTimerResponseProto;
  }
  
}
