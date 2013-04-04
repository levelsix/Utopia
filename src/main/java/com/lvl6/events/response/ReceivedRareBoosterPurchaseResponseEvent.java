package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ReceivedRareBoosterPurchaseResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ReceivedRareBoosterPurchaseResponseEvent extends NormalResponseEvent {

  private ReceivedRareBoosterPurchaseResponseProto receivedRareBoosterPurchaseResponseProto;
  
  public ReceivedRareBoosterPurchaseResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RECEIVED_RARE_BOOSTER_PURCHASE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = receivedRareBoosterPurchaseResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setReceivedRareBoosterPurchaseResponseProto(ReceivedRareBoosterPurchaseResponseProto receivedRareBoosterPurchaseResponseProto) {
    this.receivedRareBoosterPurchaseResponseProto = receivedRareBoosterPurchaseResponseProto;
  }

}
