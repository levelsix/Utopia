package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class CriticalStructureActionResponseEvent extends NonBroadcastResponseEvent {

  private CriticalStructureActionResponseProto criticalStructureActionResponseProto;
  
  public CriticalStructureActionResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CRIT_STRUCTURE_ACTION_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = criticalStructureActionResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setCriticalStructureActionResponseProto(CriticalStructureActionResponseProto criticalStructureActionResponseProto) {
    this.criticalStructureActionResponseProto = criticalStructureActionResponseProto;
  }

}
