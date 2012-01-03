package com.lvl6.server;
import java.nio.*;
import java.nio.channels.*;

import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;

public class EventWriter extends Wrap {
  //reference to game server
  private GameServer server;

  /** 
   * constructor.
   */
  public EventWriter(GameServer server, int numWorkers) {
    this.server = server;
    initWrap(numWorkers);
  }

  /** 
   * note we override the Wrap's run method here
   * doing essentially the same thing, but 
   * first we allocate a ByteBuffer for this
   * thread to use
   */
  public void run() {
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);

    ResponseEvent event;
    running = true;
    while (running) {
      try {
        if ((event = (ResponseEvent)eventQueue.deQueue()) != null) {
          processResponseEvent(event, writeBuffer);
        }
      }
      catch(InterruptedException e) {
      }
    }
  }

  /** unused */
  protected void processEvent(GameEvent event) {}

  /** 
   * our own version of processEvent that takes 
   * the additional parameter of the writeBuffer 
   */
  protected void processResponseEvent(ResponseEvent event, ByteBuffer writeBuffer) {
    NIOUtils.prepBuffer(event, writeBuffer);

    if (BroadcastResponseEvent.class.isInstance(event)) {
      int[] recipients = ((BroadcastResponseEvent)event).getRecipients();
      
      for (int i = 0; i < recipients.length; i++) {
        if (recipients[i] > 0) {
          log.info("writeEvent(): type=" + event.getType() + ", id=" + 
              recipients[i]);
          write(recipients[i], writeBuffer);
        }
      }
    }
    // Otherwise this is just a normal message, send response to sender.
    else
    {
      log.info("writeEvent: type=" + event.getType() + ", id=" + 
          event.getPlayerId());
      int playerId = event.getPlayerId();
      write(playerId, writeBuffer);
    }
  }

  /**
   * write the event to the given playerId's channel
   */
  private void write(int playerId, ByteBuffer writeBuffer) {  
    ConnectedPlayer connectedPlayer = server.getPlayerById(playerId);
    SocketChannel channel = connectedPlayer.getChannel();

    if (channel == null || !channel.isConnected()) {
      log.error("writeEvent: client channel null or not connected");
      return;
    }

    NIOUtils.channelWrite(channel, writeBuffer);
  }

}// EventWriter