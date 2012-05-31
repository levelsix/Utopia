package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.SellNormStructureRequestProto;

public class SellNormStructureRequestEvent extends RequestEvent {

  private SellNormStructureRequestProto sellNormStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      sellNormStructureRequestProto = SellNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = sellNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public SellNormStructureRequestProto getSellNormStructureRequestProto() {
    return sellNormStructureRequestProto;
  }
}
