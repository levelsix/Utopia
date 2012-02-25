package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.EnableAPNSRequestProto;

public class EnableAPNSRequestEvent extends RequestEvent {

  private EnableAPNSRequestProto enableAPNSRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      enableAPNSRequestProto = EnableAPNSRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = enableAPNSRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public EnableAPNSRequestProto getEnableAPNSRequestProto() {
    return enableAPNSRequestProto;
  }
}
