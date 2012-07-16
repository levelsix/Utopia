package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithRequestProto;

public class SubmitEquipsToBlacksmithRequestEvent extends RequestEvent {

  private SubmitEquipsToBlacksmithRequestProto submitEquipsToBlacksmithRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      submitEquipsToBlacksmithRequestProto = SubmitEquipsToBlacksmithRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = submitEquipsToBlacksmithRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public SubmitEquipsToBlacksmithRequestProto getSubmitEquipsToBlacksmithRequestProto() {
    return submitEquipsToBlacksmithRequestProto;
  }
}
