package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.EnhanceEquipRequestProto;

public class EnhanceEquipRequestEvent extends RequestEvent {

  private EnhanceEquipRequestProto enhanceEquipRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      enhanceEquipRequestProto = EnhanceEquipRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = enhanceEquipRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public EnhanceEquipRequestProto getEnhanceEquipRequestProto() {
    return enhanceEquipRequestProto;
  }
}
