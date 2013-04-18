package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PrestigeResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PrestigeResponseEvent extends NormalResponseEvent {
  
  private PrestigeResponseProto prestigeResponseProto;
  
  public PrestigeResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PRESTIGE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = prestigeResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPrestigeResponseProto(PrestigeResponseProto prestigeResponseProto) {
    this.prestigeResponseProto = prestigeResponseProto;
  }
}