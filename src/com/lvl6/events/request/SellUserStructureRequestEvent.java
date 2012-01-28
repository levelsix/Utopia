package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.SellUserStructureRequestProto;

public class SellUserStructureRequestEvent extends RequestEvent {

  private SellUserStructureRequestProto sellUserStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      sellUserStructureRequestProto = SellUserStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = sellUserStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public SellUserStructureRequestProto getSellUserStructureRequestProto() {
    return sellUserStructureRequestProto;
  }
}
