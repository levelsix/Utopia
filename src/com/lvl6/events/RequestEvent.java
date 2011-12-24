package com.lvl6.events;

import java.nio.ByteBuffer;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Events
 * or a completely different class may be used as required by a specific game.
 */

public abstract class RequestEvent extends GameEvent {
  
  public abstract void read (ByteBuffer bb);
  
}// GameEvent
