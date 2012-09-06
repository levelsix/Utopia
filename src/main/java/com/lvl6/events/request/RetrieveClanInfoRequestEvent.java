package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveClanInfoRequestProto;

public class RetrieveClanInfoRequestEvent extends RequestEvent {

  private RetrieveClanInfoRequestProto retrieveClanInfoRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveClanInfoRequestProto = RetrieveClanInfoRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveClanInfoRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveClanInfoRequestProto getRetrieveClanInfoRequestProto() {
    return retrieveClanInfoRequestProto;
  }
}
