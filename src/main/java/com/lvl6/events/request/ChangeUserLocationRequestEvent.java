package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ChangeUserLocationRequestProto;

public class ChangeUserLocationRequestEvent extends RequestEvent {

  private ChangeUserLocationRequestProto changeUserLocationRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      changeUserLocationRequestProto = ChangeUserLocationRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = changeUserLocationRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ChangeUserLocationRequestProto getChangeUserLocationRequestProto() {
    return changeUserLocationRequestProto;
  }
}
