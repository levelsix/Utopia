package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ExpansionWaitCompleteRequestProto;

public class ExpansionWaitCompleteRequestEvent extends RequestEvent {

  private ExpansionWaitCompleteRequestProto expansionWaitCompleteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      expansionWaitCompleteRequestProto = ExpansionWaitCompleteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = expansionWaitCompleteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ExpansionWaitCompleteRequestProto getExpansionWaitCompleteRequestProto() {
    return expansionWaitCompleteRequestProto;
  }
}
