package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ChangeClanDescriptionRequestProto;

public class ChangeClanDescriptionRequestEvent extends RequestEvent {

  private ChangeClanDescriptionRequestProto changeClanDescriptionRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      changeClanDescriptionRequestProto = ChangeClanDescriptionRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = changeClanDescriptionRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ChangeClanDescriptionRequestProto getChangeClanDescriptionRequestProto() {
    return changeClanDescriptionRequestProto;
  }
}
