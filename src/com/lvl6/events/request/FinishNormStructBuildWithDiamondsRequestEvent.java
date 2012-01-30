package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsRequestProto;

public class FinishNormStructBuildWithDiamondsRequestEvent extends RequestEvent {

  private FinishNormStructBuildWithDiamondsRequestProto finishNormStructBuildWithDiamondsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      finishNormStructBuildWithDiamondsRequestProto = FinishNormStructBuildWithDiamondsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = finishNormStructBuildWithDiamondsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public FinishNormStructBuildWithDiamondsRequestProto getFinishNormStructBuildWithDiamondsRequestProto() {
    return finishNormStructBuildWithDiamondsRequestProto;
  }
}
