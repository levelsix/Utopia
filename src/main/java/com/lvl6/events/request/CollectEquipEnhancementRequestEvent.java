package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CollectEquipEnhancementRequestProto;

public class CollectEquipEnhancementRequestEvent extends RequestEvent {

  private CollectEquipEnhancementRequestProto collectEquipEnhancementRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      collectEquipEnhancementRequestProto = CollectEquipEnhancementRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = collectEquipEnhancementRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CollectEquipEnhancementRequestProto getCollectEquipEnhancementRequestProto() {
    return collectEquipEnhancementRequestProto;
  }
}
