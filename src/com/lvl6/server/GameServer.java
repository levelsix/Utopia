package com.lvl6.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.*;

import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.PlayerSet;

public class GameServer extends Thread{

  // Logger
  private Logger log = Logger.getLogger(this.getClass().getSimpleName());

  // ServerSocketChannel for accepting client connections
  private ServerSocketChannel sSockChan;

  private SelectAndRead selectAndRead;
  private PlayerSet playersInAction; 
  
  // selector for multiplexing ServerSocketChannels
  private Selector selector;
  
  //whether to keep listening for new sockets
  private boolean running;

  //used for loading event controllers into hashtable
  private static final String CONTROLLER_CLASS_NAME = "EventController.class";
  private static final String CONTROLLER_CLASS_PATHNAME = "com/lvl6/server/controller/" + CONTROLLER_CLASS_NAME;
  private static final String CONTROLLER_CLASS_PREFIX = "com.lvl6.server.controller.";
  private Hashtable<EventProtocolRequest, EventController> eventControllers;

  private Hashtable<Integer, ConnectedPlayer> playersByPlayerId;
  private Hashtable<SocketChannel, Integer> channelToPlayerId;
  private Hashtable<String, SocketChannel> udidToChannel;

  private EventWriter eventWriter;

  //user specified input
  private String serverIP;
  private int portNum;
  
  //current client version to see if it is still playable
  public static float clientVersionNumber = (float) 1.0;

  public static void main(String args[]) {
    if (args.length == 2) {
      BasicConfigurator.configure();
      GameServer server = new GameServer(args[0], Integer.parseInt(args[1]));
      DBConnection.init();
      server.start();
    } else {
      System.out.println("Error in input- two arguments required: <serverip> <portnum>");
    }
  }

  public GameServer(String serverIP, int portNum) {
    if (eventControllers == null)
      eventControllers = new Hashtable<EventProtocolRequest, EventController>();
    if (playersByPlayerId == null)
      playersByPlayerId = new Hashtable<Integer, ConnectedPlayer>();
    if (channelToPlayerId == null)
      channelToPlayerId = new Hashtable<SocketChannel, Integer>();
    if (udidToChannel == null)
      udidToChannel = new Hashtable<String, SocketChannel>();
    this.serverIP = serverIP;
    this.portNum = portNum;
  }

  public void init() {
    log.info("init : Server initializing");
    loadEventControllers();
    
    initServerSocket();   //can make several of these with diff portnums in each, binded to same selector

    selectAndRead = new SelectAndRead(this);
    selectAndRead.start();
    
    eventWriter = new EventWriter(this, Globals.EVENT_WRITER_WORKERS); 
    playersInAction = new PlayerSet();
  }
  
  /**
   * pass the event on to the EventWriter
   */
  public void writeEvent(ResponseEvent e) {
    eventWriter.handleEvent(e);
  }
  
  /**
   * loop over the select() call to 
   * accept socket connections and hand them off to SelectAndRead
   */
  public void run() {
    init();
    log.info("******** GameServer running ********");
    running = true;

    while (running) {
      // note, since we only have one ServerSocket to listen to,
      // we don't need a Selector here, but we set it up for 
      // later additions such as listening on another port 
      // for administrative uses.
      try {
        // blocking select, will return when we get a new connection
        selector.select();

        // fetch the keys
        Set<SelectionKey> readyKeys = selector.selectedKeys();

        // run through the keys and process
        Iterator<SelectionKey> i = readyKeys.iterator();
        while (i.hasNext()) {
          SelectionKey key = (SelectionKey) i.next();
          i.remove();

          ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
          SocketChannel clientChannel = ssChannel.accept();

          // add to the list in SelectAndRead for processing
          selectAndRead.addNewClient(clientChannel);
          log.info("got connection from: " + clientChannel.socket().getInetAddress());
        }   
      }
      catch (IOException ioe) {
        log.warn("error during serverSocket select(): " + ioe.getMessage());
      }
      catch (Exception e) {
        log.error("exception in run()", e);
      }
    }
  }

  /**
   * Specific initialization, bind to the server port, setup the Selector, etc.
   */
  private void initServerSocket() {
    log.info("initServerSocket : Initializing server socket");

    try {
      // open a non-blocking server socket channel
      sSockChan = ServerSocketChannel.open();
      sSockChan.configureBlocking(false);

      // bind to localhost on designated port
      InetAddress addr = InetAddress.getByName(serverIP);

      log.info("binding to address: " + addr.getHostAddress());
      sSockChan.socket().bind(new InetSocketAddress(addr, portNum));

      // get a selector
      selector = Selector.open();

      // register the channel with the selector to handle accepts
      sSockChan.register(selector, SelectionKey.OP_ACCEPT);
    }
    catch (Exception e) {
      log.error("error initializing ServerSocket", e);
      System.exit(1);
    }
  }

  /**
   * finds the EventController for a given event type
   */
  public EventController getEventControllerByEventType(EventProtocolRequest eventType) {
    EventController ec = eventControllers.get(eventType);
    if (ec == null) 
      log.error("no eventcontroller for eventType: " + eventType);
    return ec;
  }
  
  /**
   *  Dynamically loads GameControllers
   */
  private void loadEventControllers() {    
    log.info("loadEventControllers : Loading event controllers");
    
    // grab all class files in the same directory as EventController
    File f = new File( this.getClass( ).getClassLoader().getResource(CONTROLLER_CLASS_PATHNAME).getPath());
    File[] files = f.getParentFile().listFiles( );

    if (files == null) {
      log.error("error getting GameController directory");
      return;
    }

    for( int i = 0; ( i < files.length); i++) {
      String file = files[i].getName( );
      if (file.indexOf( ".class") == -1)
        continue;
      if (file.equals(CONTROLLER_CLASS_NAME))
        continue;

      try {
        // grab the class
        String endOfClass = file.substring(0, file.indexOf(".class"));
        String controllerClassName = CONTROLLER_CLASS_PREFIX + endOfClass;
        log.info("loading class: " + endOfClass);

        Class<?> cl = Class.forName(controllerClassName);

        // make sure it extends GameController
        if (!EventController.class.isAssignableFrom(cl)) {
          log.warn("class file does not extend EventController: " + file);
          continue;
        }

        // get an instance and initialize
        EventController ec = (EventController) cl.newInstance();
        ec.init(this);

        // add to our controllers hash
        eventControllers.put(ec.getEventType(), ec);

        log.info("loaded controller for event: " + ec.getEventType());
      } 
      catch (Exception e) {
        log.error("Error instantiating EventController from file: " + file, e);
      }
    }
  }
  
  /** 
   * shutdown the GameServer
   */
  public void shutdown() {
    running = false;
    selector.wakeup();
  }
  
  /**
   * fetches the Player for a given playerId
   */
  public synchronized ConnectedPlayer getPlayerById(int id) {
    return playersByPlayerId.get(id);
  }
  
  /** 
   * add a player to our lists
   */
  public synchronized void addPlayer(ConnectedPlayer p) {
    channelToPlayerId.put(p.getChannel(), p.getPlayerId());
    playersByPlayerId.put(p.getPlayerId(), p);
  }

  /**
   * remove a player from our lists
   */
  public synchronized void removePlayer(SocketChannel channel) {
    try {
      playersByPlayerId.remove(channelToPlayerId.get(channel));
      channelToPlayerId.remove(channel);
    }
    catch (Exception e) {
      System.out.println("PlayersByPlayerId: "+playersByPlayerId);
      System.out.println("ChannelToPlayerId: "+channelToPlayerId);
    }
  }

  public void lockPlayer(int playerId) {
    playersInAction.addPlayer(playerId);
  }
  
  public void lockPlayers(int playerId1, int playerId2) {
    if (playerId1 > playerId2) {
      lockPlayer(playerId2);
      lockPlayer(playerId1);
    } 
    else {
      lockPlayer(playerId1);
      lockPlayer(playerId2);
    }
  }

  public void unlockPlayer(int playerId) {
    playersInAction.removePlayer(playerId);
  }
  
  public void unlockPlayers(int playerId1, int playerId2) {
    if (playerId1 > playerId2) {
      unlockPlayer(playerId2);
      unlockPlayer(playerId1);
    } 
    else {
      unlockPlayer(playerId1);
      unlockPlayer(playerId2);
    }
  }

  public void addPreDbPlayer(String udid, SocketChannel channel) {
    udidToChannel.put(udid, channel);
  }
  
  public SocketChannel removePreDbPlayer(String udid) {
    return udidToChannel.remove(udid);
  }
}
