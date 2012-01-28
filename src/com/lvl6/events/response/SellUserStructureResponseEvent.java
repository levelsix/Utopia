package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.SellUserStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SellUserStructureResponseEvent extends NonBroadcastResponseEvent {

  private SellUserStructureResponseProto sellUserStructureResponseProto;
  
  public SellUserStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SELL_USER_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = sellUserStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setSellUserStructureResponseProto(SellUserStructureResponseProto sellUserStructureResponseProto) {
    this.sellUserStructureResponseProto = sellUserStructureResponseProto;
  }

}
