package com.lvl6.scheduledtasks;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.lvl6.utils.ConnectedPlayer;


//@Component
public class HazelcastScheduledTasks {

	
	private static final Logger log = LoggerFactory.getLogger(HazelcastScheduledTasks.class);
	
	@Resource(name = "playersByPlayerId")
	protected IMap<Integer, ConnectedPlayer> playersByPlayerId;

	public IMap<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}

	public void setPlayersByPlayerId(IMap<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}
	
	
	@Autowired
	protected HazelcastInstance hazel;

	public HazelcastInstance getHazel() {
		return hazel;
	}

	public void setHazel(HazelcastInstance hazel) {
		this.hazel = hazel;
	}
	
	private int tenMinutes = 60*10*1000;
	
	@Scheduled(fixedRate=60000)
	public void cleanupPlayersByPlayerIdMap() {
		int count = 0;
		int countActive = 0;
		boolean gotLock = false;
		ILock playersCleanupLock = getHazel().getLock("playersCleanupLock");
		try {
			if(playersCleanupLock.tryLock()) {
				gotLock = true;
				for(Integer playerId : getPlayersByPlayerId().keySet()) {
					ConnectedPlayer player = getPlayersByPlayerId().get(playerId);
					if(System.currentTimeMillis() - tenMinutes > player.getLastMessageSentToServer().getTime()) {
						log.info("Player {} timed out... removing from playersByPlayerId map", player.getPlayerId());
						getPlayersByPlayerId().remove(playerId);
						count++;
					}else {
						countActive++;
					}
				}
				log.info("Removed {} players from playersByPlayerId map during cleanup", count);
				log.info("Currently {} active players", countActive);
			}
		}catch(Exception e) {
			log.error("Error cleaning up playersByPlayerId map", e);
		}finally {
			if(gotLock) {
				playersCleanupLock.forceUnlock();
			}
		}
	}
	
}
