package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveBoosterPackRequestProto;

public class RetrieveBoosterPackRequestEvent extends RequestEvent{
  private RetrieveBoosterPackRequestProto retrieveBoosterPackRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveBoosterPackRequestProto = RetrieveBoosterPackRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveBoosterPackRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveBoosterPackRequestProto getRetrieveBoosterPackRequestProto() {
    return retrieveBoosterPackRequestProto;
  }
  
}//RetrieveStaticDataForShopRequestProto