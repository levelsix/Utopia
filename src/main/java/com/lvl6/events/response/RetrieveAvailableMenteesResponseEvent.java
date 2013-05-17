package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveAvailableMenteesResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveAvailableMenteesResponseEvent extends NormalResponseEvent {
  
  private RetrieveAvailableMenteesResponseProto retrieveAvailableMenteesResponseProto;

  public RetrieveAvailableMenteesResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_AVAILABLE_MENTEES_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retrieveAvailableMenteesResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setRetrieveAvailableMenteesResponseProto(
      RetrieveAvailableMenteesResponseProto retrieveAvailableMenteesResponseProto) {
    this.retrieveAvailableMenteesResponseProto = retrieveAvailableMenteesResponseProto;
  }

  public RetrieveAvailableMenteesResponseProto getRetrieveAvailableMenteesResponseProto() {
    return retrieveAvailableMenteesResponseProto;
  }
  
}