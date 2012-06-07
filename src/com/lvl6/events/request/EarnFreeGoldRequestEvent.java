package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.EarnFreeGoldRequestProto;

public class EarnFreeGoldRequestEvent extends RequestEvent {

  private EarnFreeGoldRequestProto earnFreeGoldRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      earnFreeGoldRequestProto = EarnFreeGoldRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = earnFreeGoldRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public EarnFreeGoldRequestProto getEarnFreeGoldRequestProto() {
    return earnFreeGoldRequestProto;
  }
}
