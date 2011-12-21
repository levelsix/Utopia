package com.lvl6.events;

import java.nio.ByteBuffer;

import com.lvl6.utils.EventProtocol;
import com.lvl6.utils.NIOUtils;


/**
 * ChatEvent.java
 *
 * A basic GameEvent class, this can be extended for other Games
 * or a completely different class may be used as required by a specific game.
 */
public class ChatEvent extends BroadcastEvent {
  
  private String message;
  
  /** 
   * default contructor
   */
  public ChatEvent(){
    this.eventType = EventProtocol.C_CHAT_EVENT;
  }

  /**
   * constructor that takes eventType and message
   */
  public ChatEvent(byte type, int playerId, String message, int recipients[]){
    this.eventType = EventProtocol.S_CHAT_EVENT;
    this.message = message;
    this.recipients = recipients;
    this.numRecipients = recipients.length;
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
    
    buff.putInt(senderId);
    buff.putInt(numRecipients);
    for (int i=0;i<numRecipients;i++) 
      buff.putInt(recipients[i]);
    NIOUtils.putStr(buff, message);

    // return the length of the event, this will get inserted at the beginning of the buffer
    // in the EventWriter so the Reader knows how many bytes to read for the payload
    return buff.position() - pos;
  }

  /**
   * read the event from the given ByteBuffer
   */
  public void read(ByteBuffer buff) {
    senderId = buff.getInt();
    numRecipients = buff.getInt();
    recipients = new int[numRecipients];
    for (int i=0;i<numRecipients;i++) 
      recipients[i] = buff.getInt();
    message = NIOUtils.getStr(buff);
  }
}// ChatEvent