package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RedeemMarketplaceEarningsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RedeemMarketplaceEarningsResponseEvent extends NormalResponseEvent {

  private RedeemMarketplaceEarningsResponseProto redeemMarketplaceEarningsResponseProto;
  
  public RedeemMarketplaceEarningsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_REDEEM_MARKETPLACE_EARNINGS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = redeemMarketplaceEarningsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRedeemMarketplaceEarningsResponseProto(RedeemMarketplaceEarningsResponseProto redeemMarketplaceEarningsResponseProto) {
    this.redeemMarketplaceEarningsResponseProto = redeemMarketplaceEarningsResponseProto;
  }

}