package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetractMarketplacePostRequestProto;

public class RetractMarketplacePostRequestEvent extends RequestEvent {

  private RetractMarketplacePostRequestProto retractMarketplacePostRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retractMarketplacePostRequestProto = RetractMarketplacePostRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retractMarketplacePostRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetractMarketplacePostRequestProto getRetractMarketplacePostRequestProto() {
    return retractMarketplacePostRequestProto;
  }
}
