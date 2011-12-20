package com.lvl6.events;

import java.nio.ByteBuffer;


/**
 * ChatEvent.java
 *
 * A basic GameEvent class, this can be extended for other Games
 * or a completely different class may be used as required by a specific game.
 */
public class ChatEvent implements GameEvent {
  
  /** 
   * default contructor
   */
  public ChatEvent(){
  }

  /** 
   * constructor that takes eventType
   */
  public ChatEvent(int type) {
    this.eventType = type;
  }

  /**
   * constructor that takes eventType and message
   */
  public ChatEvent(int type, String message){
    this.eventType = type;
    this.message = message;
  }

  public void setType(int type) {
    eventType = type;
  }
  public int getType() {
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

    buff.putInt(eventType);
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
    eventType = buff.getInt();
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

}// ChatEvent