package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseBoosterPackResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseBoosterPackResponseEvent extends NormalResponseEvent {

  private PurchaseBoosterPackResponseProto purchaseBoosterPackResponseProto;
  
  public PurchaseBoosterPackResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_BOOSTER_PACK_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseBoosterPackResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseBoosterPackResponseProto(PurchaseBoosterPackResponseProto purchaseBoosterPackResponseProto) {
    this.purchaseBoosterPackResponseProto = purchaseBoosterPackResponseProto;
  }

}
