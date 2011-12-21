package com.lvl6.events;

import java.nio.ByteBuffer;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Events
 * or a completely different class may be used as required by a specific game.
 */

public abstract class GameEvent {
  
  /** event type */
  protected byte eventType;

  /** playerID that sent the message (for client msgs) */
  protected int senderId;
  
  public byte getType() {
    return eventType;
  }

  public int getPlayerId() {
    return senderId;
  }
  public void setPlayerId(int id) {
    senderId = id;
  }
  
  public abstract void convertToOutgoing();
  public abstract int write (ByteBuffer bb);
  public abstract void read (ByteBuffer bb);

}// GameEvent
