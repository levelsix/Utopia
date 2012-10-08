package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BossActionRequestProto;

public class BossActionRequestEvent extends RequestEvent {

  private BossActionRequestProto bossActionRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      bossActionRequestProto = BossActionRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = bossActionRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public BossActionRequestProto getBossActionRequestProto() {
    return bossActionRequestProto;
  }
}
