package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PrivateChatPostRequestProto;

public class PrivateChatPostRequestEvent extends RequestEvent {

  private PrivateChatPostRequestProto privateChatPostRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      privateChatPostRequestProto = PrivateChatPostRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = privateChatPostRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PrivateChatPostRequestProto getPrivateChatPostRequestProto() {
    return privateChatPostRequestProto;
  }
}
