package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteRequestProto;

public class ForgeAttemptWaitCompleteRequestEvent extends RequestEvent {

  private ForgeAttemptWaitCompleteRequestProto forgeAttemptWaitCompleteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      forgeAttemptWaitCompleteRequestProto = ForgeAttemptWaitCompleteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = forgeAttemptWaitCompleteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ForgeAttemptWaitCompleteRequestProto getForgeAttemptWaitCompleteRequestProto() {
    return forgeAttemptWaitCompleteRequestProto;
  }
}
