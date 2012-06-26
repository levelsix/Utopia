package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteRequestProto;

public class RefillStatWaitCompleteRequestEvent extends RequestEvent {

  private RefillStatWaitCompleteRequestProto refillStatWaitCompleteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      refillStatWaitCompleteRequestProto = RefillStatWaitCompleteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = refillStatWaitCompleteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RefillStatWaitCompleteRequestProto getRefillStatWaitCompleteRequestProto() {
    return refillStatWaitCompleteRequestProto;
  }
}
