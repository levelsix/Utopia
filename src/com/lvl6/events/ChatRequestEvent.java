package com.lvl6.events;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.proto.EventProto.ChatRequestProto;

/**
 * ChatEvent.java
 *
 * A basic GameEvent class, this can be extended for other Games
 * or a completely different class may be used as required by a specific game.
 */
public class ChatRequestEvent extends RequestEvent{
  private ChatRequestProto chatRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      chatRequestProto = ChatRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = chatRequestProto.getSender().getSenderId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }
  
  public String getMessage() {
    return chatRequestProto.getMessage();
  }

  public ChatRequestProto getChatRequestProto() {
    return chatRequestProto;
  }
    
}// ChatEvent