package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RefillStatWithDiamondsResponseEvent extends NonBroadcastResponseEvent {

  private RefillStatWithDiamondsResponseProto refillStatWithDiamondsResponseProto;
  
  public RefillStatWithDiamondsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_REFILL_STAT_WITH_DIAMONDS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = refillStatWithDiamondsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRefillStatWithDiamondsResponseProto(RefillStatWithDiamondsResponseProto refillStatWithDiamondsResponseProto) {
    this.refillStatWithDiamondsResponseProto = refillStatWithDiamondsResponseProto;
  }

}
