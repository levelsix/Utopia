package com.lvl6.server;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.PlayerSet;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GameServer implements InitializingBean, HazelcastInstanceAware{

	private static final int LOCK_TIMEOUT = 10000;
	public static int LOCK_WAIT_SECONDS = 60;
	
	// Logger
	private static Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	// ServerSocketChannel for accepting client connections
//	private ServerSocketChannel sSockChan;
//
//	
//	private SelectAndRead selectAndRead;
//	public void setSelectAndRead(SelectAndRead selectAndRead) {
//		this.selectAndRead = selectAndRead;
//	}
	
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

	
	
	@Resource(name="lockMap")
	IMap<String, Date> lockMap;



	public IMap<String, Date> getLockMap() {
		return lockMap;
	}

	public void setLockMap(IMap<String, Date> lockMap) {
		this.lockMap = lockMap;
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
	//private Selector selector;

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
	public static float clientVersionNumber;

	public static void main(String args[]) {
		ApplicationContext context = new FileSystemXmlApplicationContext("target/utopia-server-1.0-SNAPSHOT/WEB-INF/spring-application-context.xml");
	}

	
	
	public GameServer(String serverIP, int portNum) {
		if (eventControllers == null)
			eventControllers = new Hashtable<EventProtocolRequest, EventController>();
		this.serverIP = serverIP;
		this.portNum = portNum;
		BasicConfigurator.configure();
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Starting game server");
		clientVersionNumber = Globals.VERSION_NUMBER();
		run();
		
	}
	
	
	public void init() {
		log.info("init : Server initializing");
		loadEventControllers();
		MiscMethods.reloadAllRareChangeStaticData();
	}

	/**
	 * pass the event on to the EventWriter
	 */
	public void writeEvent(ResponseEvent e) {
		eventWriter.handleEvent(e);
	}

	

	/**
	 * pass the clan event on to the EventWriter
	 */
	public void writeClanEvent(ResponseEvent e, int clanId) {
		eventWriter.handleClanEvent(e, clanId);
	}
	
	public void writeApnsClanEvent (ResponseEvent e, int clanId){
		apnsWriter.handleClanEvent(e, clanId);
		eventWriter.handleClanEvent(e, clanId);
	}
	
	  public String serverId() {
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
	}



	/**
	 * finds the EventController for a given event type
	 * @throws Exception 
	 */
	public EventController getEventControllerByEventType(EventProtocolRequest eventType) {
		if(eventType == null) {
			throw new RuntimeException("EventProtocolRequest (eventType) is null");
		}
		if(eventControllerList.size() > eventControllers.size()) {
			loadEventControllers();
		}
		if(eventControllers.containsKey(eventType)) {
			EventController ec = eventControllers.get(eventType);
			if (ec == null) {
				log.error("no eventcontroller for eventType: " + eventType);
				throw new RuntimeException("EventController of type: "+eventType+" not found");
			}
			return ec;
		}
		throw new RuntimeException("EventController of type: "+eventType+" not found");
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


	
	public boolean lockClan(int clanId) {
		log.debug("Locking clan: "+clanId);
		if(lockMap.tryLock(clanLockName(clanId),LOCK_WAIT_SECONDS, TimeUnit.SECONDS)) {
			log.debug("Got lock for clan "+ clanId);
			lockMap.put(clanLockName(clanId), new Date());
			return true;
		}else {
			log.warn("failed to aquire lock for "+ clanLockName(clanId));
			return false;
			//throw new RuntimeException("Unable to obtain lock after "+LOCK_WAIT_SECONDS+" seconds");
		}
	}
	
	
	public void unlockClan(int clanId) {
		log.debug("Unlocking clan: "+clanId);
		try {
			String clanLockName = clanLockName(clanId);
			if(lockMap.isLocked(clanLockName)){
				lockMap.unlock(clanLockName);
			}
			log.debug("Unlocked clan "+ clanId);
			if (lockMap.containsKey(clanLockName)) {
				lockMap.remove(clanLockName);
			}
		}catch(Exception e) {
			log.error("Error unlocking clan "+clanId, e);
		}
	}
	
	protected String clanLockName(int clanId) {
		return "ClanLock: "+clanId;
	}

	
	
	
	//TODO: refactor this into a lockmap wrapper class and make it work for any lock
	//also consider refactoring playerLocks to use it
	@Scheduled(fixedDelay=LOCK_TIMEOUT)
	public void clearOldLocks(){
		long now = new Date().getTime();
		log.debug("Removing stale clan locks");
		for(String key : lockMap.keySet()){
			try {
				if(key != null && key.contains("ClanLock")) {
					long lockTime = lockMap.get(key).getTime(); 
					if(now - lockTime > LOCK_TIMEOUT){
						if(lockMap.isLocked(key)) {
							lockMap.forceUnlock(key);
						}
						lockMap.remove(key);
						log.info("Automatically removing timed out lock: "+key);
					}
				}else {
				}
			}catch(Exception e) {
				log.error("Error removing stale lock for clan "+ key, e);
			}
		}
	}
	
	
	
	
	
	
	
	
	public boolean lockPlayer(int playerId) {
		log.debug("Locking player: "+playerId);
		//Lock playerLock = hazel.getLock(playersInAction.lockName(playerId));
		if(lockMap.tryLock(playersInAction.lockName(playerId),LOCK_WAIT_SECONDS, TimeUnit.SECONDS)) {
			log.debug("Got lock for player "+ playerId);
			playersInAction.addPlayer(playerId);
			return true;
		}else {
			log.warn("failed to aquire lock for "+ playersInAction.lockName(playerId));
			throw new RuntimeException("Unable to obtain lock after "+LOCK_WAIT_SECONDS+" seconds");
		}
	}
	


	public boolean lockPlayers(int playerId1, int playerId2) {
		log.info("Locking players: "+playerId1+", "+playerId2);
		if (playerId1 == playerId2) {
			return lockPlayer(playerId1);
		}
		if (playerId1 > playerId2) {
			return lockPlayer(playerId2) && lockPlayer(playerId1);
		} else {
			return lockPlayer(playerId1) &&	lockPlayer(playerId2);
		}
	}

	public void unlockPlayer(int playerId) {
		log.debug("Unlocking player: "+playerId);
		//ILock lock = hazel.getLock(playersInAction.lockName(playerId));
		try {
			if(lockMap.isLocked(playersInAction.lockName(playerId))){
				lockMap.unlock(playersInAction.lockName(playerId));
			}
			log.debug("Unlocked player "+ playerId);
			if (playersInAction.containsPlayer(playerId)) {
				playersInAction.removePlayer(playerId);
			}
		}catch(Exception e) {
			log.error("Error unlocking player "+playerId, e);
		}
	}

	public void unlockPlayers(int playerId1, int playerId2) {
		log.info("Unlocking players: "+playerId1+", "+playerId2);
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
	
	protected HazelcastInstance hazel;
	
	@Override
	@Autowired
	public void setHazelcastInstance(HazelcastInstance instance) {
		hazel = instance;
	}

	
}
