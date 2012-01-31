package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsRequestProto;

public class FinishNormStructWaittimeWithDiamondsRequestEvent extends RequestEvent {

  private FinishNormStructWaittimeWithDiamondsRequestProto finishNormStructWaittimeWithDiamondsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      finishNormStructWaittimeWithDiamondsRequestProto = FinishNormStructWaittimeWithDiamondsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = finishNormStructWaittimeWithDiamondsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public FinishNormStructWaittimeWithDiamondsRequestProto getFinishNormStructWaittimeWithDiamondsRequestProto() {
    return finishNormStructWaittimeWithDiamondsRequestProto;
  }
}
