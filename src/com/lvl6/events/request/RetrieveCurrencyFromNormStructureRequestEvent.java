package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureRequestProto;

public class RetrieveCurrencyFromNormStructureRequestEvent extends RequestEvent{
  private RetrieveCurrencyFromNormStructureRequestProto retrieveCurrencyFromNormStructureRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveCurrencyFromNormStructureRequestProto = RetrieveCurrencyFromNormStructureRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveCurrencyFromNormStructureRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveCurrencyFromNormStructureRequestProto getRetrieveCurrencyFromNormStructureRequestProto() {
    return retrieveCurrencyFromNormStructureRequestProto;
  }
  
}//RetrieveCurrencyFromNormStructureRequestProto