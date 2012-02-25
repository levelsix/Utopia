package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseMarketplaceLicenseResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseMarketplaceLicenseResponseEvent extends NormalResponseEvent {

  private PurchaseMarketplaceLicenseResponseProto purchaseMarketplaceLicenseResponseProto;
  
  public PurchaseMarketplaceLicenseResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_MARKETPLACE_LICENSE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseMarketplaceLicenseResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseMarketplaceLicenseResponseProto(PurchaseMarketplaceLicenseResponseProto purchaseMarketplaceLicenseResponseProto) {
    this.purchaseMarketplaceLicenseResponseProto = purchaseMarketplaceLicenseResponseProto;
  }

  public PurchaseMarketplaceLicenseResponseProto getPurchaseMarketplaceLicenseResponseProto() { //required for APNS
    return purchaseMarketplaceLicenseResponseProto;
  }
  
}
