package com.lvl6.server;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.ITopic;
import com.lvl6.events.response.PurgeClientStaticDataResponseEvent;
import com.lvl6.utils.ConnectedPlayer;

public class ServerAdmin {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	
	@Resource(name="playersByPlayerId")
	Map<Integer, ConnectedPlayer> players;
	
	public Map<Integer, ConnectedPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(Map<Integer, ConnectedPlayer> players) {
		this.players = players;
	}

	@Resource(name="serverEvents")
	protected ITopic<ServerMessage> serverEvents;
		
	public ITopic<ServerMessage> getServerEvents() {
		return serverEvents;
	}

	public void setServerEvents(ITopic<ServerMessage> serverEvents) {
		this.serverEvents = serverEvents;
	}

	
	@Resource(name="eventWriter")
	protected EventWriter writer;
	

	public EventWriter getWriter() {
		return writer;
	}

	public void setWriter(EventWriter writer) {
		this.writer = writer;
	}

	public void reloadAllStaticData() {
		log.info("Reloading all static data for cluster");
		serverEvents.publish(ServerMessage.RELOAD_STATIC_DATA);
		sendPurgeStaticDataNotificationToAllClients();
	}
	
	protected void sendPurgeStaticDataNotificationToAllClients() {
		log.info("Sending purge static data notification to clients");
		Iterator<Integer> playas = players.keySet().iterator();
		while(playas.hasNext()) {
			Integer playa = playas.next();
			PurgeClientStaticDataResponseEvent pcsd = new PurgeClientStaticDataResponseEvent(playa);
			writer.processResponseEvent(pcsd);
		}
	}
	
}
