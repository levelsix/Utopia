package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ReconnectRequestProto;

public class ReconnectRequestEvent extends RequestEvent {

  private ReconnectRequestProto reconnectRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      reconnectRequestProto = ReconnectRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = reconnectRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ReconnectRequestProto getReconnectRequestProto() {
    return reconnectRequestProto;
  }
}
