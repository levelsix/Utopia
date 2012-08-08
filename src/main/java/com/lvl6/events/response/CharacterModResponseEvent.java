package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.CharacterModResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class CharacterModResponseEvent extends NormalResponseEvent {

  private CharacterModResponseProto characterModResponseProto;
  
  public CharacterModResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_CHARACTER_MOD_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = characterModResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setCharacterModResponseProto(CharacterModResponseProto characterModResponseProto) {
    this.characterModResponseProto = characterModResponseProto;
  }

}
