package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseForgeSlotResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseForgeSlotResponseEvent extends NormalResponseEvent {

  private PurchaseForgeSlotResponseProto purchaseForgeSlotResponseProto;
  
  public PurchaseForgeSlotResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ARMORY_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseForgeSlotResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseForgeSlotResponseProto(PurchaseForgeSlotResponseProto purchaseForgeSlotResponseProto) {
    this.purchaseForgeSlotResponseProto = purchaseForgeSlotResponseProto;
  }

}
