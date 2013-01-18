package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementRequestProto;

public class SubmitEquipEnhancementRequestEvent extends RequestEvent {

  private SubmitEquipEnhancementRequestProto submitEquipEnhancementRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      submitEquipEnhancementRequestProto = SubmitEquipEnhancementRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = submitEquipEnhancementRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public SubmitEquipEnhancementRequestProto getSubmitEquipEnhancementRequestProto() {
    return submitEquipEnhancementRequestProto;
  }
}
