package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class MoveUserStructureResponseEvent extends NonBroadcastResponseEvent {

  private MoveUserStructureResponseProto moveUserStructureResponseProto;
  
  public MoveUserStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_MOVE_USER_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = moveUserStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setMoveUserStructureResponseProto(MoveUserStructureResponseProto moveUserStructureResponseProto) {
    this.moveUserStructureResponseProto = moveUserStructureResponseProto;
  }

}
