package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.PurchaseGroupChatRequestProto;

public class PurchaseGroupChatRequestEvent extends RequestEvent {

  private PurchaseGroupChatRequestProto purchaseGroupChatRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      purchaseGroupChatRequestProto = PurchaseGroupChatRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = purchaseGroupChatRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public PurchaseGroupChatRequestProto getPurchaseGroupChatRequestProto() {
    return purchaseGroupChatRequestProto;
  }
}
