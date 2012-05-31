package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsRequestProto;

public class RedeemMarketplaceEarningsRequestEvent extends RequestEvent {

  private RedeemMarketplaceEarningsRequestProto redeemMarketplaceEarningsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      redeemMarketplaceEarningsRequestProto = RedeemMarketplaceEarningsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = redeemMarketplaceEarningsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RedeemMarketplaceEarningsRequestProto getRedeemMarketplaceEarningsRequestProto() {
    return redeemMarketplaceEarningsRequestProto;
  }
}
