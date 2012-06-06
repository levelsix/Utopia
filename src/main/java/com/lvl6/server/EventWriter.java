package com.lvl6.server;
import java.nio.*;
import java.nio.channels.*;

import org.apache.log4j.Logger;

import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;

public class EventWriter extends Wrap {
  //reference to game server
  private GameServer server;

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  /** 
   * constructor.
   */
  public EventWriter() {

  }


  protected void processEvent(GameEvent event) {
	  ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
	  if(event instanceof ResponseEvent)
		  processResponseEvent((ResponseEvent) event, writeBuffer);
	  
  }

  /** 
   * our own version of processEvent that takes 
   * the additional parameter of the writeBuffer 
   */
  protected void processResponseEvent(ResponseEvent event, ByteBuffer writeBuffer) {
    log.info("writer received event=" + event);
    
    NIOUtils.prepBuffer(event, writeBuffer);

    if (BroadcastResponseEvent.class.isInstance(event)) {
      int[] recipients = ((BroadcastResponseEvent)event).getRecipients();
      
      for (int i = 0; i < recipients.length; i++) {
        if (recipients[i] > 0) {
          log.info("writing broadcast event with type=" + event.getEventType() + " to players with ids " +
              recipients[i]);
          write(recipients[i], writeBuffer);
        }
      }
    }
    // Otherwise this is just a normal message, send response to sender.
    else
    {
      int playerId = ((NormalResponseEvent)event).getPlayerId();
      log.info("writing normal event with type=" + event.getEventType() + " to player with id " + playerId + ", event=" + event);
      write(playerId, writeBuffer);
    }
    
  }

  /**
   * write the event to the given playerId's channel
   */
  private void write(int playerId, ByteBuffer writeBuffer) {  
    ConnectedPlayer connectedPlayer = server.getPlayerById(playerId);
    
    if (connectedPlayer != null) {
      SocketChannel channel = connectedPlayer.getChannel();
  
      if (channel == null || !channel.isConnected()) {
        log.error("writeEvent: client channel is null or disconnected for playerId " + playerId);
        return;
      }
  
      NIOUtils.channelWrite(channel, writeBuffer, playerId);
    } else {
      log.info("playerId " + playerId + " is no longer in server"); 
    }
  }

}// EventWriter