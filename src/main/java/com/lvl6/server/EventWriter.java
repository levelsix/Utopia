package com.lvl6.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ITopic;
import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.Wrap;

public class EventWriter extends Wrap implements HazelcastInstanceAware {
	// reference to game server

	
	@Resource(name="gameEventsHandlerExecutor")
	protected Executor gameEventsExecutor;


	public Executor getGameEventsExecutor() {
		return gameEventsExecutor;
	}

	public void setGameEventsExecutor(Executor gameEventsExecutor) {
		this.gameEventsExecutor = gameEventsExecutor;
	}

	public Map<String, ConnectedPlayer> getPlayersPreDatabaseByUDID() {
		return playersPreDatabaseByUDID;
	}

	public void setPlayersPreDatabaseByUDID(
			Map<String, ConnectedPlayer> playersPreDatabaseByUDID) {
		this.playersPreDatabaseByUDID = playersPreDatabaseByUDID;
	}

	public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}

	public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}

	@Resource(name="playersPreDatabaseByUDID")
	protected Map<String, ConnectedPlayer> playersPreDatabaseByUDID;


	@Resource(name="playersByPlayerId")
	protected Map<Integer, ConnectedPlayer> playersByPlayerId;




	private static Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	/**
	 * constructor.
	 */
	public EventWriter() {

	}

	protected void processEvent(GameEvent event) {
		if (event instanceof ResponseEvent)
			processResponseEvent((ResponseEvent) event);

	}


	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	public void processResponseEvent(ResponseEvent event) {
		log.info("writer received event=" + event);
		ByteBuffer buff = getBytes(event);
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					log.info("writing broadcast event with type="+ event.getEventType() + " to players with ids "+ recipients[i]);
					ConnectedPlayer player = playersByPlayerId.get(recipients[i]);
					if(player != null){
						log.info("writing normal event with type=" + event.getEventType()+ " to player with id " + recipients[i] + ", event=" + event);
						write(buff.duplicate(), player);
					}else{
						//throw new Exception("Player "+playerId+" not found in playersByPlayerId");
						log.error("Player "+recipients[i]+" not found in playersByPlayerId");
					}
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			ConnectedPlayer player = playersByPlayerId.get(playerId);
			if(player != null){
				log.info("writing normal event with type=" + event.getEventType()+ " to player with id " + playerId + ", event=" + event);
				write(buff, player);
			}else{
				//throw new Exception("Player "+playerId+" not found in playersByPlayerId");
				log.error("Player "+playerId+" not found in playersByPlayerId");
			}
		}

	}


	public void processPreDBResponseEvent(ResponseEvent event, String udid) {
		ConnectedPlayer player = playersPreDatabaseByUDID.get(udid);
		ByteBuffer bytes = getBytes(event);
		write(bytes, player);
	}

	protected ByteBuffer getBytes(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		return writeBuffer;
	}

	/**
	 * write the event to the given playerId's channel
	 */
	private void write(ByteBuffer event, ConnectedPlayer player) {
		ITopic<Message<?>> serverOutboundMessages = hazel.getTopic(
				ServerInstance.getOutboundMessageTopicForServer(
						player.getServerHostName()));
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("ip_connection_id", player.getIp_connection_id());
		if(player.getPlayerId() != 0) {
			headers.put("playerId", player.getPlayerId());
		}
		byte[] bArray = new byte[event.remaining()];
		event.get(bArray);
		Message<byte[]> msg = new GenericMessage<byte[]>(bArray, headers);
		serverOutboundMessages.publish(msg);
	}

	protected HazelcastInstance hazel;
	@Override
	@Autowired
	public void setHazelcastInstance(HazelcastInstance instance) {
		hazel = instance;
	}

}// EventWriter