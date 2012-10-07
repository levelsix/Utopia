package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PickLockBoxRequestProto;

public class PickLockBoxRequestEvent extends RequestEvent {

  private PickLockBoxRequestProto pickLockBoxRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      pickLockBoxRequestProto = PickLockBoxRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = pickLockBoxRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PickLockBoxRequestProto getPickLockBoxRequestProto() {
    return pickLockBoxRequestProto;
  }
}
