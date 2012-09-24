package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PlayThreeCardMonteRequestProto;

public class PlayThreeCardMonteRequestEvent extends RequestEvent {

  private PlayThreeCardMonteRequestProto playThreeCardMonteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      playThreeCardMonteRequestProto = PlayThreeCardMonteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = playThreeCardMonteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PlayThreeCardMonteRequestProto getPlayThreeCardMonteRequestProto() {
    return playThreeCardMonteRequestProto;
  }
}
