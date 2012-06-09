package com.lvl6.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;

public class EventWriter extends Wrap {
	// reference to game server

	@Autowired
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

	@Autowired
	protected Map<String, ConnectedPlayer> playersPreDatabaseByUDID;


	@Autowired
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
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					log.info("writing broadcast event with type="+ event.getEventType() + " to players with ids "+ recipients[i]);
					ConnectedPlayer player = playersByPlayerId.get(recipients[i]);
					write(event, player);
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			ConnectedPlayer player = playersByPlayerId.get(playerId);
			log.info("writing normal event with type=" + event.getEventType()+ " to player with id " + playerId + ", event=" + event);
			write(event, player);
		}

	}
	
	
	public void processPreDBResponseEvent(ResponseEvent event, String udid) {
		ConnectedPlayer player = playersPreDatabaseByUDID.get(udid);
		write(event, player);
	}
	
	

	/**
	 * write the event to the given playerId's channel
	 */
	private void write(ResponseEvent event, ConnectedPlayer player) {
		ITopic<Message<?>> serverOutboundMessages = Hazelcast.getTopic(ServerInstance.getOutboundMessageTopicForServer(player.getServerHostName()));
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("ip_connection_id", player.getIp_connection_id());
		Message<ResponseEvent> msg = new GenericMessage<ResponseEvent>(event);
		serverOutboundMessages.publish(msg);
	}

}// EventWriter