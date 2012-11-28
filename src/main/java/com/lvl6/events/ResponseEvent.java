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
	protected int tag;
     
  public EventProtocolResponse getEventType() {
    return eventType;
  }
    
  public abstract int write (ByteBuffer bb);

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

//	@Override
//	public String toString() {
//		return ReflectionToStringBuilder.toString(this);
//	}
}// GameEvent
