package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ArmoryRequestProto;

public class ArmoryRequestEvent extends RequestEvent {

  private ArmoryRequestProto armoryRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      armoryRequestProto = ArmoryRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = armoryRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ArmoryRequestProto getArmoryRequestProto() {
    return armoryRequestProto;
  }
}
