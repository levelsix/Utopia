package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseNormStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseNormStructureResponseEvent extends NormalResponseEvent {

  private PurchaseNormStructureResponseProto purchaseNormStructureResponseProto;
  
  public PurchaseNormStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_NORM_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseNormStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseNormStructureResponseProto(PurchaseNormStructureResponseProto purchaseNormStructureResponseProto) {
    this.purchaseNormStructureResponseProto = purchaseNormStructureResponseProto;
  }

}
