package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ThreeCardMonteRequestProto;

public class ThreeCardMonteRequestEvent extends RequestEvent {

  private ThreeCardMonteRequestProto threeCardMonteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      threeCardMonteRequestProto = ThreeCardMonteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = threeCardMonteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ThreeCardMonteRequestProto getThreeCardMonteRequestProto() {
    return threeCardMonteRequestProto;
  }
}
