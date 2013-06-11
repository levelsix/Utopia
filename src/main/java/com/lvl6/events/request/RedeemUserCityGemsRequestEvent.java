package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RedeemUserCityGemsRequestProto;

public class RedeemUserCityGemsRequestEvent extends RequestEvent {
  
  private RedeemUserCityGemsRequestProto redeemUserCityGemsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      redeemUserCityGemsRequestProto = RedeemUserCityGemsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = redeemUserCityGemsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RedeemUserCityGemsRequestProto getRedeemUserCityGemsRequestProto() {
    return redeemUserCityGemsRequestProto;
  }
}