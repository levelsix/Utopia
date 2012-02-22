package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseFromMarketplaceResponseEvent extends NormalResponseEvent {

  private PurchaseFromMarketplaceResponseProto purchaseFromMarketplaceResponseProto;
  
  public PurchaseFromMarketplaceResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_FROM_MARKETPLACE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseFromMarketplaceResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseFromMarketplaceResponseProto(PurchaseFromMarketplaceResponseProto purchaseFromMarketplaceResponseProto) {
    this.purchaseFromMarketplaceResponseProto = purchaseFromMarketplaceResponseProto;
  }

}
