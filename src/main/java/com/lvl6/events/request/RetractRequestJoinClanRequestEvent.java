package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetractRequestJoinClanRequestProto;

public class RetractRequestJoinClanRequestEvent extends RequestEvent {

  private RetractRequestJoinClanRequestProto retractRequestJoinClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retractRequestJoinClanRequestProto = RetractRequestJoinClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retractRequestJoinClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetractRequestJoinClanRequestProto getRetractRequestJoinClanRequestProto() {
    return retractRequestJoinClanRequestProto;
  }
}
