package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RequestJoinClanRequestProto;

public class RequestJoinClanRequestEvent extends RequestEvent {

  private RequestJoinClanRequestProto requestJoinClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      requestJoinClanRequestProto = RequestJoinClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = requestJoinClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RequestJoinClanRequestProto getRequestJoinClanRequestProto() {
    return requestJoinClanRequestProto;
  }
}
