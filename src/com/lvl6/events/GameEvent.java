package com.lvl6.events;

import java.nio.ByteBuffer;

/**
 * GameEvent.java
 *
 * Interface for GameEvents, all event classes must implement this interface.
 */
public interface GameEvent {
    public byte getType();
    public void setType(byte type);
    
    public String getGameName();
    public void setGameName(String gameName);
	
    public String getMessage();
    public void setMessage(String message);
    
    public String getPlayerId();
    public void setPlayerId(String id);
    
    public String getSessionId();
    public void setSessionId(String id);

    public String[] getRecipients();
    public void setRecipients(String[] recipients);

    public void read(ByteBuffer buff);
    public int write(ByteBuffer buff);
}

