package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ClericHealRequestProto;

public class ClericHealRequestEvent extends RequestEvent {

  private ClericHealRequestProto clericHealRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      clericHealRequestProto = ClericHealRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = clericHealRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ClericHealRequestProto getClericHealRequestProto() {
    return clericHealRequestProto;
  }
}
