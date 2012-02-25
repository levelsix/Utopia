package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseRequestProto;

public class PurchaseMarketplaceLicenseRequestEvent extends RequestEvent {

  private PurchaseMarketplaceLicenseRequestProto purchaseMarketplaceLicenseRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseMarketplaceLicenseRequestProto = PurchaseMarketplaceLicenseRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseMarketplaceLicenseRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseMarketplaceLicenseRequestProto getPurchaseMarketplaceLicenseRequestProto() {
    return purchaseMarketplaceLicenseRequestProto;
  }
}
