package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PrestigeRequestProto;

public class PrestigeRequestEvent extends RequestEvent {
  
  private PrestigeRequestProto prestigeRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      prestigeRequestProto = PrestigeRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = prestigeRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PrestigeRequestProto getPrestigeRequestProto() {
    return prestigeRequestProto;
  }
}