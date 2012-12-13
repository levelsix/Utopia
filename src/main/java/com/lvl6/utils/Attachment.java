package com.lvl6.utils;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

/**
 * Attachment.java
 *
 * This class is used as the Key attachment for each channel registered
 * with the Selector.
 * It holds the temporary incoming data and checks the completeness 
 * of the header and payload.
 */
public class Attachment {
  /** number of bytes in the network event header */
  public static final int HEADER_SIZE = 12; 

  private Logger log = LoggerFactory.getLogger(Attachment.class);
  
  /** event type for this message */
  public EventProtocolRequest eventType;
  
  /** size in bytes of the payload (GameEvent) */
  public int payloadSize;
  
  /** size in bytes of the payload (GameEvent) */
  public int tag;

  /** do we have a full header yet? */
  private boolean gotHeader;

  /** buffer used to hold data as it's read from the channel */
  public ByteBuffer readBuff; 

  /** temporary storage of the payload before it is read into an event  */
  public byte payload[];
  
  protected ByteOrder getByteOrder() {
	  return ByteOrder.BIG_ENDIAN;
  }
  
  public void setPayload(byte[] bytes) {
	  payload = bytes;
	  payloadSize = bytes.length;
  }

  /** 
   * constructor. initializes the payload array and the read buffer 
   */
  public Attachment () {
    payload = new byte[Globals.MAX_EVENT_SIZE];
    readBuff = ByteBuffer.allocateDirect(Globals.NET_BUFFER_SIZE);
    readBuff.order(getByteOrder());
  }

  /** 
   * checks if a full event has been read
   * @return true if the event is ready, otherwise false
   */
  public boolean eventReady() throws IllegalArgumentException {
    if (checkHeader() && checkPayload())
      return true;
    else
      return false;
  }

  /**
   * reset the attachment to prepare for reading the next event
   */
  public void reset() {
    gotHeader = false;
  }

  /**
   * Checks if we have a full header yet 
   * @return true if the header is fully available, otherwise false
   */
  private boolean checkHeader() throws IllegalArgumentException {
    if (gotHeader) return true;
    if (readBuff.remaining() >= HEADER_SIZE) {

      // read the header info
      eventType = EventProtocolRequest.valueOf(readBuff.getInt());
      tag = readBuff.getInt();
      payloadSize = readBuff.getInt();
      log.debug("Read event type: "+eventType+" and size: "+payloadSize);

      // check bounds on the payload
      if (payloadSize > Globals.MAX_EVENT_SIZE) 
        throw new IllegalArgumentException("Header specifies payload size (" + 
            payloadSize + ") greater than MAX_EVENT_SIZE(" + 
            Globals.MAX_EVENT_SIZE + ")");
      gotHeader = true;
      return true;
    }
    else {
      return false;
    }
  }

  /** 
   * check for a complete payload
   */
  private boolean checkPayload() {
    if (readBuff.remaining() >= payloadSize) {
      try {
        readBuff.get(payload, 0, payloadSize);
      }
      catch (BufferUnderflowException bue) {
        log.error("buffer underflow", bue);
      }
      return true;
    }
    else {
      return false;
    }
  }
}// Attachment
