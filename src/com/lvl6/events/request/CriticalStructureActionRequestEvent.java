package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto;

public class CriticalStructureActionRequestEvent extends RequestEvent {

  private CriticalStructureActionRequestProto criticalStructureActionRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      criticalStructureActionRequestProto = CriticalStructureActionRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = criticalStructureActionRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CriticalStructureActionRequestProto getCriticalStructureActionRequestProto() {
    return criticalStructureActionRequestProto;
  }
}
