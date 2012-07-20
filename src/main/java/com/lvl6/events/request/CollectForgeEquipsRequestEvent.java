package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CollectForgeEquipsRequestProto;

public class CollectForgeEquipsRequestEvent extends RequestEvent {

  private CollectForgeEquipsRequestProto collectForgeEquipsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      collectForgeEquipsRequestProto = CollectForgeEquipsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = collectForgeEquipsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CollectForgeEquipsRequestProto getCollectForgeEquipsRequestProto() {
    return collectForgeEquipsRequestProto;
  }
}
