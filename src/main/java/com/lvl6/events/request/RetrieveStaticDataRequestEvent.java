package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveStaticDataRequestProto;

public class RetrieveStaticDataRequestEvent extends RequestEvent {

  private RetrieveStaticDataRequestProto retrieveStaticDataRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveStaticDataRequestProto = RetrieveStaticDataRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveStaticDataRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveStaticDataRequestProto getRetrieveStaticDataRequestProto() {
    return retrieveStaticDataRequestProto;
  }
}
