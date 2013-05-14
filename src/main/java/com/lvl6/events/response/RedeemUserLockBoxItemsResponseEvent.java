package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RedeemUserLockBoxItemsResponseEvent extends NormalResponseEvent {
  
  private RedeemUserLockBoxItemsResponseProto redeemUserLockBoxItemsResponseProto;
  
  public RedeemUserLockBoxItemsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_REDEEM_USER_LOCK_BOX_ITEMS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = redeemUserLockBoxItemsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRedeemUserLockBoxItemsResponseProto(RedeemUserLockBoxItemsResponseProto redeemUserLockBoxItemsResponseProto) {
    this.redeemUserLockBoxItemsResponseProto = redeemUserLockBoxItemsResponseProto;
  }
}