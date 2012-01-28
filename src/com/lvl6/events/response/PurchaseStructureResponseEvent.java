package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.PurchaseStructureResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseStructureResponseEvent extends NonBroadcastResponseEvent {

  private PurchaseStructureResponseProto purchaseStructureResponseProto;
  
  public PurchaseStructureResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_STRUCTURE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseStructureResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseStructureResponseProto(PurchaseStructureResponseProto purchaseStructureResponseProto) {
    this.purchaseStructureResponseProto = purchaseStructureResponseProto;
  }

}
