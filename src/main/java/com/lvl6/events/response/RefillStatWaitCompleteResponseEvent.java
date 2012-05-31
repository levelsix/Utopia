package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RefillStatWaitCompleteResponseEvent extends NormalResponseEvent {

  private RefillStatWaitCompleteResponseProto refillStatWaitCompleteResponseProto;
  
  public RefillStatWaitCompleteResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_REFILL_STAT_WAIT_COMPLETE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = refillStatWaitCompleteResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRefillStatWaitCompleteResponseProto(RefillStatWaitCompleteResponseProto refillStatWaitCompleteResponseProto) {
    this.refillStatWaitCompleteResponseProto = refillStatWaitCompleteResponseProto;
  }

}
