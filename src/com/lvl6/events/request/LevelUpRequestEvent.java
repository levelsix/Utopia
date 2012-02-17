package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.LevelUpRequestProto;

public class LevelUpRequestEvent extends RequestEvent {

  private LevelUpRequestProto levelUpRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      levelUpRequestProto = LevelUpRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = levelUpRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public LevelUpRequestProto getLevelUpRequestProto() {
    return levelUpRequestProto;
  }
}
