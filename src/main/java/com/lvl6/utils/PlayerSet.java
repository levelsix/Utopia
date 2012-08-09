package com.lvl6.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;

public class PlayerSet implements HazelcastInstanceAware {

	
	Logger log = Logger.getLogger(PlayerSet.class);
	
	private IMap<Integer, PlayerInAction> players;

	public IMap<Integer, PlayerInAction> getPlayers() {
		return players;
	}

	public void setPlayers(IMap<Integer, PlayerInAction> players) {
		this.players = players;
	}

	
	/**
	 * lock a player
	 * 
	 * @throws InterruptedException
	 */
	public void addPlayer(int playerId) {
		players.put(playerId, new PlayerInAction(playerId), 30, TimeUnit.SECONDS);
	}

	public void removePlayer(int playerId) {
		if(containsPlayer(playerId))
			players.remove(playerId);
	}

	public boolean containsPlayer(int playerId) {
		return players.containsKey(playerId);
	}
	
	
	
	//@Scheduled(fixedDelay=60000)
	public void clearOldLocks(){
		long now = new Date().getTime();
		log.debug("Removing stale player locks");
		for(Integer player:players.keySet()){
			PlayerInAction play = players.get(player);
			if(now - play.getLockTime().getTime() > 60000){
				ILock playerLock = hazel.getLock(play.getPlayerId());
				playerLock.forceUnlock();
				removePlayer(player);
				log.info("Automatically removing timed out lock for player: "+play.getPlayerId());
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
