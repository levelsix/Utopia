package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.SellNormStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SellNormStructureResponseEvent extends NonBroadcastResponseEvent {

  private SellNormStructureResponseProto sellNormStructureResponseProto;
  
  public SellNormStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SELL_NORM_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = sellNormStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setSellNormStructureResponseProto(SellNormStructureResponseProto sellNormStructureResponseProto) {
    this.sellNormStructureResponseProto = sellNormStructureResponseProto;
  }

}
