package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureRequestProto;

public class MoveOrRotateNormStructureRequestEvent extends RequestEvent {

  private MoveOrRotateNormStructureRequestProto moveOrRotateNormStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      moveOrRotateNormStructureRequestProto = MoveOrRotateNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = moveOrRotateNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public MoveOrRotateNormStructureRequestProto getMoveOrRotateNormStructureRequestProto() {
    return moveOrRotateNormStructureRequestProto;
  }
}
