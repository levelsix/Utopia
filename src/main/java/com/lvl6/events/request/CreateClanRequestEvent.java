package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CreateClanRequestProto;

public class CreateClanRequestEvent extends RequestEvent {

  private CreateClanRequestProto createClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      createClanRequestProto = CreateClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = createClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CreateClanRequestProto getCreateClanRequestProto() {
    return createClanRequestProto;
  }
}
