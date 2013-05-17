package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.MenteeBecameAvailableResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class MenteeBecameAvailableResponseEvent extends NormalResponseEvent {
  
  private MenteeBecameAvailableResponseProto menteeBecameAvailableResponseProto;

  public MenteeBecameAvailableResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_MENTEE_BECAME_AVAILABLE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = menteeBecameAvailableResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setMenteeBecameAvailableResponseProto(
      MenteeBecameAvailableResponseProto menteeBecameAvailableResponseProto) {
    this.menteeBecameAvailableResponseProto = menteeBecameAvailableResponseProto;
  }

  public MenteeBecameAvailableResponseProto getMenteeBecameAvailableResponseProto() {
    return menteeBecameAvailableResponseProto;
  }
  
}