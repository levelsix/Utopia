package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.LeaveClanRequestProto;

public class LeaveClanRequestEvent extends RequestEvent {

  private LeaveClanRequestProto leaveClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      leaveClanRequestProto = LeaveClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = leaveClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public LeaveClanRequestProto getLeaveClanRequestProto() {
    return leaveClanRequestProto;
  }
}
