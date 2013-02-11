package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseBoosterPackRequestProto;

public class PurchaseBoosterPackRequestEvent extends RequestEvent {

  private PurchaseBoosterPackRequestProto purchaseBoosterPackRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseBoosterPackRequestProto = PurchaseBoosterPackRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseBoosterPackRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseBoosterPackRequestProto getPurchaseBoosterPackRequestProto() {
    return purchaseBoosterPackRequestProto;
  }
}
