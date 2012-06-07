package com.lvl6.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

import com.lvl6.events.PreDatabaseRequestEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.info.User;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.Attachment;
import com.lvl6.utils.ConnectedPlayer;

public class SelectAndRead extends Thread{
  //Logger
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
      Set<SelectionKey> readyKeys = selector.selectedKeys();

      Iterator<SelectionKey> i = readyKeys.iterator();
      while (i.hasNext()) {
        SelectionKey key = i.next();
        i.remove();
        SocketChannel channel = (SocketChannel) key.channel();
        Attachment attachment = (Attachment) key.attachment();

        try {
          // read from the channel
          long nbytes = channel.read(attachment.readBuff);
          // check for end-of-stream condition
          if (nbytes == -1) {
            removePlayerFromServer(channel);
            channel.close();
            log.info("disconnect: " + channel.socket().getInetAddress() + 
                ", end-of-stream");
          }

            if (attachment.readBuff.position() >= Attachment.HEADER_SIZE) {
              attachment.readBuff.flip();

              // read as many events as are available in the buffer
              while(attachment.eventReady()) {
                RequestEvent event = getEvent(attachment);
                log.info("Recieved event from client: "+event.getPlayerId());
                delegateEvent(event, channel, attachment.eventType);
                attachment.reset();
              }
              // prepare for more channel reading
              attachment.readBuff.compact();
            }
        }
        catch (IOException ioe) {
          log.warn("IOException during read(), closing channel:" + channel.socket().getInetAddress());
          removePlayerFromServer(channel);
          channel.close();
        }
        catch (Exception e) {
          log.warn("Exception: " + e);
          removePlayerFromServer(channel);
          channel.close();
        }
      }
    }
    catch (IOException ioe2) {
      log.warn("IOException during select(): " + ioe2.getMessage());
    }
    catch (Exception e) {
      log.error("exception during select()", e);
      e.printStackTrace();
    }
  }

  private void removePlayerFromServer(SocketChannel channel) {
    int playerId = server.getPlayerIdOnChannel(channel);
    if (playerId > 0) {
      server.lockPlayer(playerId);
      try {
        User user = UserRetrieveUtils.getUserById(playerId);
        if (user != null) {
          if (!user.updateLastlogout(new Timestamp(new Date().getTime()))) {
            log.error("problem with updating user's last logout time for user " + playerId);
          }
        }
      } catch (Exception e) {
        log.error("exception in updating user logout", e);
      } finally {
        server.unlockPlayer(playerId); 
        server.removePlayer(channel);
      }
    } else {
      log.error("cannot update last logout because there is no player on channel " + channel);
    }
  }

  /**
   * read an event from the attachment's payload
   */
  private RequestEvent getEvent(Attachment attachment) {
    RequestEvent event = null;
    ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(attachment.payload, attachment.payloadSize));

    // get the controller and tell it to instantiate an event for us
    EventController ec = server.getEventControllerByEventType(attachment.eventType);

    if (ec == null) {
      return null;
    }
    event = ec.createRequestEvent();
    event.setTag(attachment.tag);

    // read the event from the payload
    event.read(bb); 
    return event;
  }

  /**
   * pass off an event to the appropriate GameController
   * based on the GameName of the event
   */
  private void delegateEvent(RequestEvent event, SocketChannel channel, EventProtocolRequest eventType) {
    if (event != null && eventType.getNumber() < 0) {
      log.error("the event type is < 0");
      return;
    }

    EventController ec = server.getEventControllerByEventType(eventType);
    if (ec == null) {
      log.error("No EventController for eventType: " + eventType);
      return;
    }

    ConnectedPlayer p = server.getPlayerById(event.getPlayerId());
    if (p != null) {
      if (p.getChannel() != channel) {
        log.warn("player " + p.getPlayerId() + " is on a new channel, must be reconnected.");
        p.setChannel(channel);
        server.removePlayer(p.getChannel());
        server.addPlayer(p);
      }
    }
    else {
      // first time we see a playerId, create the Player object
      // and populate the channel, and also add to our lists
      if (event.getPlayerId() > 0) {
        p = new ConnectedPlayer();
        p.setPlayerId(event.getPlayerId());
        p.setChannel(channel);
        server.addPlayer(p);
        log.debug("delegate event, new player created and channel set, player:" + 
            p.getPlayerId() + ", channel: " + channel);
      } else if (event.getPlayerId() == -1){
        // This is for the case before the tutorial has started
        // Save the socket into another array so that controller can retrieve later
        String udid = ((PreDatabaseRequestEvent)event).getUdid();
        server.addPreDbPlayer(udid, channel);
        log.debug("new pre-db player, udid: "+ udid);
      }
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
          log.info("Adding new client connection to selector"+clientChannel.hashCode());
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
