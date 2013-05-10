package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RedeemUserLockBoxItemsRequestProto;

public class RedeemUserLockBoxItemsRequestEvent extends RequestEvent {
  
  private RedeemUserLockBoxItemsRequestProto redeemUserLockBoxItemsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      redeemUserLockBoxItemsRequestProto = RedeemUserLockBoxItemsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = redeemUserLockBoxItemsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RedeemUserLockBoxItemsRequestProto getRedeemUserLockBoxItemsRequestProto() {
    return redeemUserLockBoxItemsRequestProto;
  }
}