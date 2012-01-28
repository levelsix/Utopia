package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.MoveNormStructureRequestProto;

public class MoveNormStructureRequestEvent extends RequestEvent {

  private MoveNormStructureRequestProto moveNormStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      moveNormStructureRequestProto = MoveNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = moveNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public MoveNormStructureRequestProto getMoveNormStructureRequestProto() {
    return moveNormStructureRequestProto;
  }
}
