package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.MoveNormStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class MoveNormStructureResponseEvent extends NonBroadcastResponseEvent {

  private MoveNormStructureResponseProto moveNormStructureResponseProto;
  
  public MoveNormStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_MOVE_NORM_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = moveNormStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setMoveNormStructureResponseProto(MoveNormStructureResponseProto moveNormStructureResponseProto) {
    this.moveNormStructureResponseProto = moveNormStructureResponseProto;
  }

}
