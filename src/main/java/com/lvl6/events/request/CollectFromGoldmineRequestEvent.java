package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CollectFromGoldmineRequestProto;

public class CollectFromGoldmineRequestEvent extends RequestEvent {

  private CollectFromGoldmineRequestProto collectFromGoldmineRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      collectFromGoldmineRequestProto = CollectFromGoldmineRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = collectFromGoldmineRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CollectFromGoldmineRequestProto getCollectFromGoldmineRequestProto() {
    return collectFromGoldmineRequestProto;
  }
}
