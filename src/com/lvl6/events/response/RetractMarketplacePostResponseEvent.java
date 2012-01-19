package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.RetractMarketplacePostResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetractMarketplacePostResponseEvent extends NonBroadcastResponseEvent {

  private RetractMarketplacePostResponseProto retractMarketplacePostResponseProto;
  
  public RetractMarketplacePostResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RETRACT_POST_FROM_MARKETPLACE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retractMarketplacePostResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRetractMarketplacePostResponseProto(RetractMarketplacePostResponseProto retractMarketplacePostResponseProto) {
    this.retractMarketplacePostResponseProto = retractMarketplacePostResponseProto;
  }

}
