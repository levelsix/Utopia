package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BootPlayerFromClanRequestProto;

public class BootPlayerFromClanRequestEvent extends RequestEvent {

  private BootPlayerFromClanRequestProto bootPlayerFromClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      bootPlayerFromClanRequestProto = BootPlayerFromClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = bootPlayerFromClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public BootPlayerFromClanRequestProto getBootPlayerFromClanRequestProto() {
    return bootPlayerFromClanRequestProto;
  }
}
