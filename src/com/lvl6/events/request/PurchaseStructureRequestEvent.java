package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseStructureRequestProto;

public class PurchaseStructureRequestEvent extends RequestEvent {

  private PurchaseStructureRequestProto purchaseStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseStructureRequestProto = PurchaseStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseStructureRequestProto getPurchaseStructureRequestProto() {
    return purchaseStructureRequestProto;
  }
}
