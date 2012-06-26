package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.NormStructWaitCompleteRequestProto;

public class NormStructWaitCompleteRequestEvent extends RequestEvent {

  private NormStructWaitCompleteRequestProto normStructWaitCompleteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      normStructWaitCompleteRequestProto = NormStructWaitCompleteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = normStructWaitCompleteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public NormStructWaitCompleteRequestProto getNormStructWaitCompleteRequestProto() {
    return normStructWaitCompleteRequestProto;
  }
}
