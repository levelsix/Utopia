package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseNormStructureRequestProto;

public class PurchaseNormStructureRequestEvent extends RequestEvent {

  private PurchaseNormStructureRequestProto purchaseNormStructureRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseNormStructureRequestProto = PurchaseNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseNormStructureRequestProto getPurchaseNormStructureRequestProto() {
    return purchaseNormStructureRequestProto;
  }
}
