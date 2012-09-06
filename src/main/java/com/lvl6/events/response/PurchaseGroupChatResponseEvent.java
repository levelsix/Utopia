package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurchaseGroupChatResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurchaseGroupChatResponseEvent extends NormalResponseEvent {

  private PurchaseGroupChatResponseProto purchaseGroupChatResponseProto;
  
  public PurchaseGroupChatResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURCHASE_GROUP_CHAT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purchaseGroupChatResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurchaseGroupChatResponseProto(PurchaseGroupChatResponseProto purchaseGroupChatResponseProto) {
    this.purchaseGroupChatResponseProto = purchaseGroupChatResponseProto;
  }

  public PurchaseGroupChatResponseProto getPurchaseGroupChatResponseProto() { //required for APNS
    return purchaseGroupChatResponseProto;
  }
  
}
