package com.lvl6.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.hazelcast.core.Hazelcast;
import com.lvl6.events.ResponseEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.PlayerSet;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GameServer extends Thread implements InitializingBean{

	// Logger
	private static Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	// ServerSocketChannel for accepting client connections
	private ServerSocketChannel sSockChan;

	
	private SelectAndRead selectAndRead;
	public void setSelectAndRead(SelectAndRead selectAndRead) {
		this.selectAndRead = selectAndRead;
	}
	
	@Autowired
	protected ServerInstance serverInstance;
	
	
	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	public void setServerInstance(ServerInstance serverInstance) {
		this.serverInstance = serverInstance;
	}

	@Resource(name="playersPreDatabaseByUDID")
	Map<String, ConnectedPlayer> playersPreDatabaseByUDID;


	public Map<String, ConnectedPlayer> getPlayersPreDatabaseByUDID() {
		return playersPreDatabaseByUDID;
	}

	public void setPlayersPreDatabaseByUDID(
			Map<String, ConnectedPlayer> playersPreDatabaseByUDID) {
		this.playersPreDatabaseByUDID = playersPreDatabaseByUDID;
	}

	@Resource(name="playersInAction")
	private PlayerSet playersInAction;

	public PlayerSet getPlayersInAction() {
		return playersInAction;
	}

	public void setPlayersInAction(PlayerSet playersInAction) {
		this.playersInAction = playersInAction;
	}
	
	
	@Autowired
	protected List<EventController> eventControllerList;
	public void setEventControllerList(List<EventController> eventControllerList) {
		this.eventControllerList = eventControllerList;
	}


	// selector for multiplexing ServerSocketChannels
	private Selector selector;

	// whether to keep listening for new sockets
	private boolean running;


	private Hashtable<EventProtocolRequest, EventController> eventControllers;

	@Resource(name="playersByPlayerId")
	Map<Integer, ConnectedPlayer> playersByPlayerId;
	
	
//	private Hashtable<SocketChannel, Integer> channelToPlayerId;
//	private Hashtable<String, SocketChannel> udidToChannel;

	
	public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}

	public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}

	@Autowired	
	private EventWriter eventWriter;
	public void setEventWriter(EventWriter eventWriter) {
		this.eventWriter = eventWriter;
	}

	@Autowired
	private APNSWriter apnsWriter;
	public void setApnsWriter(APNSWriter apnsWriter) {
		this.apnsWriter = apnsWriter;
	}



	// user specified input
	private String serverIP;
	private int portNum;
	
	protected boolean block = true;

	public boolean isBlock() {
		return block;
	}

	public void setBlock(boolean block) {
		this.block = block;
	}

	// current client version to see if it is still playable
	public static float clientVersionNumber = 1.0f;

	public static void main(String args[]) {
		//if (args.length == 2) {
			ApplicationContext context = new FileSystemXmlApplicationContext("target/utopia-server-1.0-SNAPSHOT/WEB-INF/spring-application-context.xml");
//			GameServer server = new GameServer(args[0],
//					Integer.parseInt(args[1]));
//			DBConnection.get().init();
//			MiscMethods.reloadAllRareChangeStaticData();
		//} else {
		//	System.out
		//			.println("Error in input- two arguments required: <serverip> <portnum>");
		//}
	}

	
	
	public GameServer(String serverIP, int portNum) {
		if (eventControllers == null)
			eventControllers = new Hashtable<EventProtocolRequest, EventController>();
//		if (playersByPlayerId == null)
//			playersByPlayerId = new Hashtable<Integer, ConnectedPlayer>();
//		if (channelToPlayerId == null)
//			channelToPlayerId = new Hashtable<SocketChannel, Integer>();
//		if (udidToChannel == null)
//			udidToChannel = new Hashtable<String, SocketChannel>();
		this.serverIP = serverIP;
		this.portNum = portNum;
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Starting game server");
		run();
	}
	
	
	public void init() {
		log.info("init : Server initializing");
		loadEventControllers();

//		initServerSocket(); // can make several of these with diff portnums in
//							// each, binded to same selector
//
//		selectAndRead = new SelectAndRead(this);
//		selectAndRead.start();
		MiscMethods.reloadAllRareChangeStaticData();
		// playersInAction = new PlayerSet();
	}

	/**
	 * pass the event on to the EventWriter
	 */
	public void writeEvent(ResponseEvent e) {
		eventWriter.handleEvent(e);
	}

	
	
	  public String serverId() throws FileNotFoundException {
		  return getServerInstance().serverId();
	  }
	
	
	/**
	 * pass the event on to the EventWriter
	 */
	public void writePreDBEvent(ResponseEvent e, String udid) {
		eventWriter.processPreDBResponseEvent(e, udid);
	}
	
	/**
	 * pass the event on to the APNSWriter
	 */
	public void writeAPNSNotificationOrEvent(ResponseEvent e) {
		apnsWriter.handleEvent(e);
	}

	/**
	 * loop over the select() call to accept socket connections and hand them
	 * off to SelectAndRead
	 */
	public void run() {
		init();
		log.info("******** GameServer running ********");
		running = true;
		if(block)
		while (running) {
			// note, since we only have one ServerSocket to listen to,
			// we don't need a Selector here, but we set it up for
			// later additions such as listening on another port
			// for administrative uses.
			try {
				// blocking select, will return when we get a new connection
				log.info("Waiting for incoming socket connection");
				int channelkey = selector.select();
				log.info("Channel connected: "+channelkey);
				// fetch the keys
				Set<SelectionKey> readyKeys = selector.selectedKeys();

				// run through the keys and process
				Iterator<SelectionKey> i = readyKeys.iterator();
				while (i.hasNext()) {
					SelectionKey key = (SelectionKey) i.next();
					i.remove();

					ServerSocketChannel ssChannel = (ServerSocketChannel) key
							.channel();
					SocketChannel clientChannel = ssChannel.accept();

					// add to the list in SelectAndRead for processing
					selectAndRead.addNewClient(clientChannel);
					log.info("received connection from: "
							+ clientChannel.socket().getInetAddress());
				}
			} catch (IOException ioe) {
				log.warn("error during serverSocket select(): "
						+ ioe.getMessage());
			} catch (Exception e) {
				log.error("exception in run()", e);
			}
		}
	}

	/**
	 * Specific initialization, bind to the server port, setup the Selector,
	 * etc.
	 */
/*	private void initServerSocket() {
		log.info("initServerSocket : Initializing server socket");

		try {
			// open a non-blocking server socket channel
			sSockChan = ServerSocketChannel.open();
			sSockChan.configureBlocking(false);

			//bind to external ip on designated port
			InetAddress addr = InetAddress.getByName("0.0.0.0");

			log.info("binding to address: " + addr.getHostAddress()+" port: "+portNum);
			sSockChan.socket().bind(new InetSocketAddress(addr, portNum));

			// get a selector
			selector = Selector.open();

			// register the channel with the selector to handle accepts
			sSockChan.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			log.error("error initializing ServerSocket", e);
			System.exit(1);
		}
	}*/

	/**
	 * finds the EventController for a given event type
	 */
	public EventController getEventControllerByEventType(
			EventProtocolRequest eventType) {
		EventController ec = eventControllers.get(eventType);
		if (ec == null)
			log.error("no eventcontroller for eventType: " + eventType);
		return ec;
	}

	/**
	 * Dynamically loads GameControllers
	 */
	private void loadEventControllers() {
		log.info("Adding event controllers to eventControllers controllerType-->controller map");
		for(EventController ec: eventControllerList) {
			eventControllers.put(ec.getEventType(), ec);
		}
	}


	/**
	 * shutdown the GameServer
	 * @throws IOException 
	 */
	public void shutdown() throws IOException {
		running = false;
//		selector.wakeup();
//		sSockChan.close();
	}

	/**
	 * fetches the Player for a given playerId
	 */
	public ConnectedPlayer getPlayerById(int id) {
		return playersByPlayerId.get(id);
	}
	
	public ConnectedPlayer getPlayerByUdId(String id) {
		return playersPreDatabaseByUDID.get(id);
	}

//	public synchronized SocketChannel getChannelForUdid(String udid) {
//		return udidToChannel.get(udid);
//	}

	/**
	 * add a player to our lists
	 */
//	public synchronized void addPlayer(ConnectedPlayer p) {
//		//channelToPlayerId.put(p.getChannel(), p.getPlayerId());
//		playersByPlayerId.put(p.getPlayerId(), p);
//	}

	/**
	 * remove a player from our lists
	 */
//	public synchronized void removePlayer(SocketChannel channel) {
//		try {
//			playersByPlayerId.remove(channelToPlayerId.get(channel));
//			channelToPlayerId.remove(channel);
//		} catch (Exception e) {
//			log.error("problem with removing player from game on channel "
//					+ channel + ". ", e);
//		}
//	}

	// returns -1 if he's not in there
//	public synchronized int getPlayerIdOnChannel(SocketChannel channel) {
//		if (channelToPlayerId.keySet().contains(channel)) {
//			return channelToPlayerId.get(channel);
//		}
//		log.error("no player on channel " + channel);
//		return -1;
//	}

	public void lockPlayer(int playerId) {
		Lock playerLock = Hazelcast.getLock(playerId);
		playerLock.lock();
		playersInAction.addPlayer(playerId);
	}

	public void lockPlayers(int playerId1, int playerId2) {
		if (playerId1 == playerId2) {
			lockPlayer(playerId1);
			return;
		}
		if (playerId1 > playerId2) {
			lockPlayer(playerId2);
			lockPlayer(playerId1);
		} else {
			lockPlayer(playerId1);
			lockPlayer(playerId2);
		}
	}

	public void unlockPlayer(int playerId) {
		Lock lock = Hazelcast.getLock(playerId);
		lock.unlock();
		if (playersInAction.containsPlayer(playerId))
			playersInAction.removePlayer(playerId);
	}

	public void unlockPlayers(int playerId1, int playerId2) {
		if (playerId1 > playerId2) {
			unlockPlayer(playerId2);
			unlockPlayer(playerId1);
		} else {
			unlockPlayer(playerId1);
			unlockPlayer(playerId2);
		}
	}

	public Set<Integer> getConnectedPlayerIds(){
		return playersByPlayerId.keySet();
	}
	
	public ConnectedPlayer removePreDbPlayer(String udid) {
		return playersPreDatabaseByUDID.remove(udid);
	}
	
}
