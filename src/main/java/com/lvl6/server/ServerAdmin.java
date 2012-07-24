package com.lvl6.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.lvl6.eventhandlers.HazelInstanceListener;
import com.lvl6.events.response.PurgeClientStaticDataResponseEvent;
import com.lvl6.proto.EventProto.PurgeClientStaticDataResponseProto;
import com.lvl6.utils.ConnectedPlayer;

public class ServerAdmin implements MessageListener<ServerMessage> {
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
	protected HazelInstanceListener hazelInstances;
	
	@Resource(name="playersByPlayerId")
	Map<Integer, ConnectedPlayer> players;

	@Resource(name="serverEvents")
	protected ITopic<ServerMessage> serverEvents;
	
	@Resource(name="staticDataReloadDone")
	protected ITopic<ServerMessage> staticDataReloadDone;
	
	@Resource(name="eventWriter")
	protected EventWriter writer;

	@Autowired
	protected HazelcastInstance hazel;
	
	
	
	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}

	public ITopic<ServerMessage> getStaticDataReloadDone() {
		return staticDataReloadDone;
	}

	public void setStaticDataReloadDone(ITopic<ServerMessage> staticDataReloadDone) {
		this.staticDataReloadDone = staticDataReloadDone;
	}
	
	public Map<Integer, ConnectedPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(Map<Integer, ConnectedPlayer> players) {
		this.players = players;
	}

	public ITopic<ServerMessage> getServerEvents() {
		return serverEvents;
	}

	public void setServerEvents(ITopic<ServerMessage> serverEvents) {
		this.serverEvents = serverEvents;
	}
	
	

	
	public HazelInstanceListener getHazelInstances() {
		return hazelInstances;
	}

	public void setHazelInstances(HazelInstanceListener hazelInstances) {
		this.hazelInstances = hazelInstances;
	}


	public EventWriter getWriter() {
		return writer;
	}

	public void setWriter(EventWriter writer) {
		this.writer = writer;
	}

	
	protected Integer instanceCountForDataReload = 0;
	protected Integer instancesDoneReloadingCount = 0;
	protected ILock instancesReloadingLock;
	public void reloadAllStaticData() {
		instancesDoneReloadingCount = 0;
		instanceCountForDataReload = getHazelInstances().getInstances().size();
		log.info("Reloading all static data for cluster instances: "+instanceCountForDataReload);
		instancesReloadingLock = hazel.getLock(ServerMessage.RELOAD_STATIC_DATA);
		getStaticDataReloadDone().addMessageListener(this);
		serverEvents.publish(ServerMessage.RELOAD_STATIC_DATA);
	}
	
	protected void sendPurgeStaticDataNotificationToAllClients() {
		Set<Integer> keySet = players.keySet();
		if(keySet != null) {
			Iterator<Integer> playas = keySet.iterator();
			log.info("Sending purge static data notification to clients: "+keySet.size());
			while(playas.hasNext()) {
				Integer playa = playas.next();
				PurgeClientStaticDataResponseEvent pcsd = new PurgeClientStaticDataResponseEvent(playa);
				pcsd.setPurgeClientStaticDataResponseProto(PurgeClientStaticDataResponseProto.newBuilder().setSenderId(playa).build());
				writer.processResponseEvent(pcsd);
			}
		}
	}

	@Override
	public void onMessage(Message<ServerMessage> msg) {
		if(msg.getMessageObject().equals(ServerMessage.DONE_RELOADING_STATIC_DATA)) {
			instancesDoneReloadingCount++;
			log.info("Instance done reloading static data: {}/{}", instancesDoneReloadingCount, instanceCountForDataReload);
			if(instancesDoneReloadingCount > instanceCountForDataReload || instancesDoneReloadingCount > getHazelInstances().getInstances().size()) {
				log.info("All instances done reloading static data");
				getStaticDataReloadDone().removeMessageListener(this);
				sendPurgeStaticDataNotificationToAllClients();
				instancesReloadingLock.unlock();
				instancesReloadingLock = null;
			}
		}
	}
	
}
