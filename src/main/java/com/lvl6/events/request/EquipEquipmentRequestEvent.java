package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.EquipEquipmentRequestProto;

public class EquipEquipmentRequestEvent extends RequestEvent {

  private EquipEquipmentRequestProto equipEquipmentRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      equipEquipmentRequestProto = EquipEquipmentRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = equipEquipmentRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public EquipEquipmentRequestProto getEquipEquipmentRequestProto() {
    return equipEquipmentRequestProto;
  }
}
