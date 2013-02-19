package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ResetBoosterPackRequestProto;

public class ResetBoosterPackRequestEvent extends RequestEvent{
  private ResetBoosterPackRequestProto resetBoosterPackRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      resetBoosterPackRequestProto = ResetBoosterPackRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = resetBoosterPackRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ResetBoosterPackRequestProto getResetBoosterPackRequestProto() {
    return resetBoosterPackRequestProto;
  }
  
}//RetrieveStaticDataForShopRequestProto