package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveStaticDataForShopRequestProto;

public class RetrieveStaticDataForShopRequestEvent extends RequestEvent{
  private RetrieveStaticDataForShopRequestProto retrieveStaticDataForShopRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveStaticDataForShopRequestProto = RetrieveStaticDataForShopRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveStaticDataForShopRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveStaticDataForShopRequestProto getRetrieveStaticDataForShopRequestProto() {
    return retrieveStaticDataForShopRequestProto;
  }
  
}//RetrieveStaticDataForShopRequestProto