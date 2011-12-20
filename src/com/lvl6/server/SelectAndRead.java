package com.lvl6.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.Attachment;
import com.lvl6.utils.Player;

public class SelectAndRead extends Thread{
  //Logger
  private Logger log = Logger.getLogger(this.getClass().getName());

  //Tracks pending SocketChannel connections
  private LinkedList<SocketChannel> newClients;

  // the selector, multiplexes access to client channels
  private Selector selector;

  //Reference to the server
  private GameServer server;

  public SelectAndRead (GameServer server){
    this.server = server;
    newClients = new LinkedList<SocketChannel>();
  }

  /** 
   * Adds to the list of pending clients
   */
  public void addNewClient(SocketChannel clientChannel) {
    synchronized (newClients) {
      newClients.addLast(clientChannel);
    }

    // force selector to return so our new client can get in the loop right away
    selector.wakeup();
  }

  /** 
   * loop forever, first doing our select() 
   * then check for new connections
   */
  public void run () {
    try {
      selector = Selector.open();
      while (true) {
        select();
        checkNewConnections();
      }
    }
    catch (IOException e) {
      log.fatal("exception while opening Selector", e);
    }
  }
  
  
  /** 
   * do our select, read from the channels
   * and hand off events to GameControllers
   */
  private void select() {
    try {
      // this is a blocking select call but will 
      // be interrupted when new clients come in
      selector.select();
      Set readyKeys = selector.selectedKeys();

      Iterator i = readyKeys.iterator();
      while (i.hasNext()) {
        SelectionKey key = (SelectionKey) i.next();
        i.remove();
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        try {
          // read from the channel
          long nbytes = channel.read(attachment.readBuff);
          // check for end-of-stream condition
          if (nbytes == -1) {
            log.info("disconnect: " + channel.socket().getInetAddress() + 
                ", end-of-stream");
            channel.close();
          }

          // check for a complete event
          try {
            if (attachment.readBuff.position() >= attachment.HEADER_SIZE) {
              attachment.readBuff.flip();

              // read as many events as are available in the buffer
              while(attachment.eventReady()) {
                GameEvent event = getEvent(attachment);
                delegateEvent(event, channel);
                attachment.reset();
              }
              // prepare for more channel reading
              attachment.readBuff.compact();
            }
          }
          catch (IllegalArgumentException e) {
            log.error("illegal argument exception", e);
          }
        }
        catch (IOException ioe) {
          log.warn("IOException during read(), closing channel:" + channel.socket().getInetAddress());
          channel.close();
        }
      }
    }
    catch (IOException ioe2) {
      log.warn("IOException during select(): " + ioe2.getMessage());
    }
    catch (Exception e) {
      log.error("exception during select()", e);
    }
  }

  /**
   * read an event from the attachment's payload
   */
  private GameEvent getEvent(Attachment attachment) {
    GameEvent event = null;
    ByteBuffer bb = ByteBuffer.wrap(attachment.payload);

    // get the controller and tell it to instantiate an event for us
    EventController ec = server.getEventControllerByEventType(attachment.eventType);
    
    if (ec == null) {
      return null;
    }
    event = ec.createEvent();

    // read the event from the payload
    event.read(bb); 
    return event;
  }

  /**
   * pass off an event to the appropriate GameController
   * based on the GameName of the event
   */
  private void delegateEvent(GameEvent event, SocketChannel channel) {
    if (event != null && event.getType() < 0) {
      log.error("the event type is < 0");
      return;
    }

    EventController ec = server.getEventControllerByEventType(event.getType());
    if (ec == null) {
      log.error("No EventController for eventType: " + event.getType());
      return;
    }

    Player p = GameServer.getPlayerById(event.getPlayerId());
    if (p != null) {
      if (p.getChannel() != channel) {
        log.warn("player is on a new channel, must be reconnect.");
        p.setChannel(channel);
      }
    }
    else {
      // first time we see a playerId, create the Player object
      // and populate the channel, and also add to our lists
      p = new Player();
      if (event.getPlayerId() > 0) {
        p.setPlayerId(event.getPlayerId());
      } else {
        //TODO: get player id from db
      }      
      p.setChannel(channel);
      GameServer.addPlayer(p);
      log.debug("delegate event, new player created and channel set, player:" + 
          p.getPlayerId() + ", channel: " + channel);
    }
    
    ec.handleEvent(event);
  }

  /**
   * check for new connections
   * and register them with the selector
   */
  private void checkNewConnections() {
    synchronized(newClients) {
      while (newClients.size() > 0) {
        try {
          SocketChannel clientChannel = newClients.removeFirst();
          clientChannel.configureBlocking( false);
          clientChannel.register(selector, SelectionKey.OP_READ, new Attachment());
        }
        catch (ClosedChannelException cce) {
          log.error("checkNewConnections - channel closed", cce);
        }
        catch (IOException ioe) {
          log.error("checkNewConnections - ioexception on clientChannel", ioe);
        }
      }
    }
  }
  
}
