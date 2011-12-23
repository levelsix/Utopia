package com.lvl6.events;

import java.nio.ByteBuffer;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Events
 * or a completely different class may be used as required by a specific game.
 */

public abstract class ResponseEvent extends GameEvent {
  
  /** event type */
  protected byte eventType;
  
  /** player id from db */
  protected int playerId;
  
  public byte getType() {
    return eventType;
  }

  public int getPlayerId() {
    return playerId;
  }
    
  public abstract int write (ByteBuffer bb);

}// GameEvent
