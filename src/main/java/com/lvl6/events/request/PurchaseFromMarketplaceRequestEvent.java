package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceRequestProto;

public class PurchaseFromMarketplaceRequestEvent extends RequestEvent {

  private PurchaseFromMarketplaceRequestProto purchaseFromMarketplaceRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseFromMarketplaceRequestProto = PurchaseFromMarketplaceRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseFromMarketplaceRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseFromMarketplaceRequestProto getPurchaseFromMarketplaceRequestProto() {
    return purchaseFromMarketplaceRequestProto;
  }
}
