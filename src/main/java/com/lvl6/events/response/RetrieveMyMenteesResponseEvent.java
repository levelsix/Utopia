package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveMyMenteesResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveMyMenteesResponseEvent extends NormalResponseEvent {
  
  private RetrieveMyMenteesResponseProto retrieveMyMenteesResponseProto;

  public RetrieveMyMenteesResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_MY_MENTEES;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retrieveMyMenteesResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setRetrieveMyMenteesResponseProto(
      RetrieveMyMenteesResponseProto retrieveMyMenteesResponseProto) {
    this.retrieveMyMenteesResponseProto = retrieveMyMenteesResponseProto;
  }

  public RetrieveMyMenteesResponseProto getRetrieveMyMenteesResponseProto() {
    return retrieveMyMenteesResponseProto;
  }
  
}