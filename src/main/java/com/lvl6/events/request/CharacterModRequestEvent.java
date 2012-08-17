package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.CharacterModRequestProto;

public class CharacterModRequestEvent extends RequestEvent {

  private CharacterModRequestProto characterModRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      characterModRequestProto = CharacterModRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = characterModRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public CharacterModRequestProto getCharacterModRequestProto() {
    return characterModRequestProto;
  }
}
