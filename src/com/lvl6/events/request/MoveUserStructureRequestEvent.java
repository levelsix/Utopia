package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.MoveUserStructureRequestProto;

public class MoveUserStructureRequestEvent extends RequestEvent {

  private MoveUserStructureRequestProto moveUserStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      moveUserStructureRequestProto = MoveUserStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = moveUserStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public MoveUserStructureRequestProto getMoveUserStructureRequestProto() {
    return moveUserStructureRequestProto;
  }
}
