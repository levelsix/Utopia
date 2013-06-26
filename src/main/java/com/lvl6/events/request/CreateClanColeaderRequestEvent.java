package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CreateClanColeaderRequestProto;

public class CreateClanColeaderRequestEvent extends RequestEvent {

  private CreateClanColeaderRequestProto createClanColeaderRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      createClanColeaderRequestProto = CreateClanColeaderRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = createClanColeaderRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CreateClanColeaderRequestProto getCreateClanColeaderRequestProto() {
    return createClanColeaderRequestProto;
  }
}