package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteRequestProto;

public class RetrieveThreeCardMonteRequestEvent extends RequestEvent {

  private RetrieveThreeCardMonteRequestProto retrieveThreeCardMonteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveThreeCardMonteRequestProto = RetrieveThreeCardMonteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveThreeCardMonteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveThreeCardMonteRequestProto getRetrieveThreeCardMonteRequestProto() {
    return retrieveThreeCardMonteRequestProto;
  }
}
