package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.LeaveClanResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class LeaveClanResponseEvent extends NormalResponseEvent {

  private LeaveClanResponseProto leaveClanResponseProto;
  
  public LeaveClanResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_LEAVE_CLAN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = leaveClanResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setLeaveClanResponseProto(LeaveClanResponseProto leaveClanResponseProto) {
    this.leaveClanResponseProto = leaveClanResponseProto;
  }

}
