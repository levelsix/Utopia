package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsRequestProto;

public class FinishForgeAttemptWaittimeWithDiamondsRequestEvent extends RequestEvent {

  private FinishForgeAttemptWaittimeWithDiamondsRequestProto finishForgeAttemptWaittimeWithDiamondsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      finishForgeAttemptWaittimeWithDiamondsRequestProto = FinishForgeAttemptWaittimeWithDiamondsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = finishForgeAttemptWaittimeWithDiamondsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public FinishForgeAttemptWaittimeWithDiamondsRequestProto getFinishForgeAttemptWaittimeWithDiamondsRequestProto() {
    return finishForgeAttemptWaittimeWithDiamondsRequestProto;
  }
}
