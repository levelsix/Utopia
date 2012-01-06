package com.lvl6.trash;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ClericCostCalculateRequestProto;

public class ClericCostCalculateRequestEvent extends RequestEvent{
  private ClericCostCalculateRequestProto clericCostCalcRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      clericCostCalcRequestProto = ClericCostCalculateRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = clericCostCalcRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }
  
  public ClericCostCalculateRequestProto getClericCostCalcRequestProto() {
    return clericCostCalcRequestProto;
  }
  
}//ClericCostCalculateRequestEvent