package com.lvl6.utils;

import java.util.Set;

public class PlayerSet {


	private Set<Integer> players;

	public Set<Integer> getPlayers() {
		return players;
	}

	public void setPlayers(Set<Integer> players) {
		this.players = players;
	}

	
	/**
	 * lock a player
	 * 
	 * @throws InterruptedException
	 */
	public void addPlayer(int playerId) {
		while (players.contains(playerId)) {
			try {
				wait();
			} catch (InterruptedException e) {
				// Continue waiting??
			}
		}
		players.add(playerId);
	}

	public void removePlayer(int playerId) {
		players.remove(playerId);
	}

	public boolean containsPlayer(int playerId) {
		return players.contains(playerId);
	}

}// EventQueue
