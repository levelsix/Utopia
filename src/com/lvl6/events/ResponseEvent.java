package com.lvl6.events;

import java.nio.ByteBuffer;

import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Events
 * or a completely different class may be used as required by a specific game.
 */

public abstract class ResponseEvent extends GameEvent{
  
  /** event type */
  protected EventProtocolResponse eventType;
     
  public EventProtocolResponse getEventType() {
    return eventType;
  }
    
  public abstract int write (ByteBuffer bb);

}// GameEvent
