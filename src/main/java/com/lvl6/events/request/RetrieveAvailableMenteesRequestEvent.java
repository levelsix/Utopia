package com.lvl6.events.request;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveAvailableMenteesRequestProto;

public class RetrieveAvailableMenteesRequestEvent extends RequestEvent {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  private RetrieveAvailableMenteesRequestProto retrieveAvailableMenteesRequestProto;
  
  @Override
  public void read(ByteBuffer bb) {
    try {
      retrieveAvailableMenteesRequestProto = RetrieveAvailableMenteesRequestProto.parseFrom(
          ByteString.copyFrom(bb));
    } catch (InvalidProtocolBufferException e) {
      log.error("unexpected error: unknown request proto.", e);
    }
  }
  
  public RetrieveAvailableMenteesRequestProto getRetrieveAvailableMenteesRequestProto() {
    return retrieveAvailableMenteesRequestProto;
  }
  
}