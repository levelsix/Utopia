package com.lvl6.events;

import java.nio.ByteBuffer;

import com.lvl6.utils.NIOUtils;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Events
 * or a completely different class may be used as required by a specific game.
 */
public abstract class GameEventDefault implements GameEvent {
  
  /** event type */
  protected byte eventType;

  /** playerID that sent the message (for client msgs) */
  protected String playerId;

  /** player's session id */
  protected String sessionId;

  /** gameID that the event belongs to, if any */
  protected int gameId = -1;

  /** gameName that the event belongs to */
  protected String  gameName;

  /** # of recipients */
  protected int numRecipients;

  /** array of event recipient playerIDs */
  protected String[] recipients;

  /** chat message or other command specific string */
  protected String message;

  /** 
   * default contructor
   */
  public GameEventDefault(){
  }

  /** 
   * constructor that takes eventType
   */
  public GameEventDefault(byte type) {
    this.eventType = type;
  }

  /**
   * constructor that takes eventType and message
   */
  public GameEventDefault(byte type, String message){
    this.eventType = type;
    this.message = message;
  }

  public void setType(byte type) {
    eventType = type;
  }
  public byte getType() {
    return eventType;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }
  
  public String getGameName() {
    return gameName;
  }

  public String getMessage() {
    return message;
  }
  
  public void setMessage(String message) {
    this.message = message;
  }

  public String getPlayerId() {
    return playerId;
  }
  public void setPlayerId(String id) {
    playerId = id;
  }

  public String getSessionId() {
    return sessionId;
  }
  public void setSessionId(String id) {
    sessionId = id;
  }

  public String[] getRecipients() {
    return recipients;
  }
  
  public void setRecipients(String[] recipients) {
    this.recipients = recipients;
    numRecipients = recipients.length;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    int pos = buff.position();

    buff.put(eventType);
    NIOUtils.putStr(buff, playerId);
    NIOUtils.putStr(buff, sessionId);
    buff.putInt(gameId);
    NIOUtils.putStr(buff, gameName);
    buff.putInt(numRecipients);
    for (int i=0;i<numRecipients;i++) 
      NIOUtils.putStr(buff, recipients[i]);
    NIOUtils.putStr(buff, message);

    // return the length of the event, this will get inserted at the beginning of the buffer
    // in the EventWriter so the Reader knows how many bytes to read for the payload
    return buff.position() - pos;
  }

  /**
   * read the event from the given ByteBuffer
   */
  public void read(ByteBuffer buff) {
    eventType = buff.get();
    playerId = NIOUtils.getStr(buff);
    sessionId = NIOUtils.getStr(buff);
    gameId = buff.getInt();
    gameName = NIOUtils.getStr(buff);
    numRecipients = buff.getInt();
    recipients = new String[numRecipients];
    for (int i=0;i<numRecipients;i++) 
      recipients[i] = NIOUtils.getStr(buff);
    message = NIOUtils.getStr(buff);
  }

}// GameEvent
