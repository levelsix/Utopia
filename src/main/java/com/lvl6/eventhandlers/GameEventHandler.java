package com.lvl6.eventhandlers;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.IMap;
import com.lvl6.events.PreDatabaseRequestEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.ConnectedPlayer;

public class GameEventHandler extends AbstractGameEventHandler {
	private static final int DEFAULT_TTL = 3;

	static Logger log = Logger.getLogger(GameEventHandler.class);

	@Resource(name = "playersByPlayerId")
	IMap<Integer, ConnectedPlayer> playersByPlayerId;

	@Resource(name = "playersPreDatabaseByUDID")
	IMap<String, ConnectedPlayer> playersPreDatabaseByUDID;

	public IMap<String, ConnectedPlayer> getPlayersPreDatabaseByUDID() {
		return playersPreDatabaseByUDID;
	}

	public void setPlayersPreDatabaseByUDID(
			IMap<String, ConnectedPlayer> playersPreDatabaseByUDID) {
		this.playersPreDatabaseByUDID = playersPreDatabaseByUDID;
	}

	public IMap<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}

	public void setPlayersByPlayerId(
			IMap<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}

	/**
	 * pass off an event to the appropriate GameController based on the GameName
	 * of the event
	 * 
	 * @throws FileNotFoundException
	 */
	@Override
	protected void delegateEvent(byte[] bytes, RequestEvent event,
			String ip_connection_id, EventProtocolRequest eventType) {
		if (event != null && eventType.getNumber() < 0) {
			log.error("the event type is < 0");
			return;
		}
		EventController ec = server.getEventControllerByEventType(eventType);
		if (ec == null) {
			log.error("No EventController for eventType: " + eventType);
			return;
		}
		updatePlayerToServerMaps(event, ip_connection_id);
		ec.handleEvent(event);
	}

	protected void updatePlayerToServerMaps(RequestEvent event,	String ip_connection_id) {
		log.debug("Updating player to server maps for player: "	+ event.getPlayerId());
		if (playersByPlayerId.containsKey(event.getPlayerId())) {
			ConnectedPlayer p = playersByPlayerId.get(event.getPlayerId());
			if (p != null) {
				p.setLastMessageSentToServer(new Date());
				//if (!p.getIp_connection_id().equals(ip_connection_id)	|| !p.getServerHostName().equals(server.serverId())) {
					//log.debug("Player is connected to a new socket or server");
					p.setIp_connection_id(ip_connection_id);
					p.setServerHostName(server.serverId());
				//}
				playersByPlayerId.put(event.getPlayerId(), p, DEFAULT_TTL, TimeUnit.MINUTES);
			} else {
				addNewConnection(event, ip_connection_id);
			}
		} else {
			addNewConnection(event, ip_connection_id);
		}

	}

	protected void addNewConnection(RequestEvent event, String ip_connection_id) {
		ConnectedPlayer newp = new ConnectedPlayer();
		newp.setIp_connection_id(ip_connection_id);
		newp.setServerHostName(server.serverId());
		if (event.getPlayerId() != -1) {
			log.info("Player logged on: " + event.getPlayerId());
			newp.setPlayerId(event.getPlayerId());
			playersByPlayerId.put(event.getPlayerId(), newp, DEFAULT_TTL,TimeUnit.MINUTES);
		} else {
			newp.setUdid(((PreDatabaseRequestEvent) event).getUdid());
			getPlayersPreDatabaseByUDID().put(newp.getUdid(), newp, DEFAULT_TTL, TimeUnit.MINUTES);
			log.info("New player with UdId: " + newp.getUdid());
		}
	}

}
