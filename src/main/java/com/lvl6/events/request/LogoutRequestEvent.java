package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.LogoutRequestProto;

public class LogoutRequestEvent extends RequestEvent {

  private LogoutRequestProto logoutRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      logoutRequestProto = LogoutRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = logoutRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public LogoutRequestProto getLogoutRequestProto() {
    return logoutRequestProto;
  }
}
