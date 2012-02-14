package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseCityExpansionRequestProto;

public class PurchaseCityExpansionRequestEvent extends RequestEvent {

  private PurchaseCityExpansionRequestProto purchaseCityExpansionRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseCityExpansionRequestProto = PurchaseCityExpansionRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseCityExpansionRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseCityExpansionRequestProto getPurchaseCityExpansionRequestProto() {
    return purchaseCityExpansionRequestProto;
  }
}
