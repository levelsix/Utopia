package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RedeemUserCityGemsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RedeemUserCityGemsResponseEvent extends NormalResponseEvent {
  
  private RedeemUserCityGemsResponseProto redeemUserCityGemsResponseProto;
  
  public RedeemUserCityGemsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_REDEEM_USER_CITY_GEMS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = redeemUserCityGemsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRedeemUserCityGemsResponseProto(RedeemUserCityGemsResponseProto redeemUserCityGemsResponseProto) {
    this.redeemUserCityGemsResponseProto = redeemUserCityGemsResponseProto;
  }
}