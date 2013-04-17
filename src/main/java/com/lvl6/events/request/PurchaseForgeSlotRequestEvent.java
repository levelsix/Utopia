package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseForgeSlotRequestProto;

public class PurchaseForgeSlotRequestEvent extends RequestEvent {

  private PurchaseForgeSlotRequestProto purchaseForgeSlotRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseForgeSlotRequestProto = PurchaseForgeSlotRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseForgeSlotRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseForgeSlotRequestProto getPurchaseForgeSlotRequestProto() {
    return purchaseForgeSlotRequestProto;
  }
}
