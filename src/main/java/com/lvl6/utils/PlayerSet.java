package com.lvl6.utils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

public class PlayerSet implements HazelcastInstanceAware {

	
	Logger log = Logger.getLogger(PlayerSet.class);
	
	protected HazelcastInstance hzInstance;
	
	private Set<PlayerInAction> players;

	public Set<PlayerInAction> getPlayers() {
		return players;
	}

	public void setPlayers(Set<PlayerInAction> players) {
		this.players = players;
	}

	
	/**
	 * lock a player
	 * 
	 * @throws InterruptedException
	 */
	public void addPlayer(int playerId) {
//		while (players.contains(playerId)) {
//			try {
//				wait();
//			} catch (InterruptedException e) {
//				// Continue waiting??
//			}
//		}
		players.add(new PlayerInAction(playerId));
	}

	public void removePlayer(int playerId) {
		players.remove(playerId);
	}

	public boolean containsPlayer(int playerId) {
		return players.contains(playerId);
	}
	
	
	
	@Scheduled(fixedDelay=60000)
	public void clearOldLocks(){
		long now = new Date().getTime();
		log.debug("Removing stale player locks");
		for(PlayerInAction player:players){
			if(now - player.getLockTime().getTime() > 60000){
				Lock playerLock = hazel.getLock(player.getPlayerId());
				playerLock.unlock();
				players.remove(player);
			}
		}
	}
	
	protected HazelcastInstance hazel;
	@Override
	@Autowired
	public void setHazelcastInstance(HazelcastInstance instance) {
		hazel = instance;
	}


}// EventQueue
