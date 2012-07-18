package com.lvl6.utils;

import java.util.Map;

import javax.annotation.Resource;

import com.lvl6.ui.admin.components.ApplicationStats;

public class ApplicationUtils {
	
	@Resource(name="playersByPlayerId")
	Map<Integer, ConnectedPlayer> players;

	protected Map<Integer, ConnectedPlayer> getPlayers() {
		return players;
	}
	public void setPlayers(Map<Integer, ConnectedPlayer> players) {
		this.players = players;
	}



	public ApplicationStats getStats() {
		ApplicationStats stats = new ApplicationStats();
		stats.setConnectedPlayersCount(players.size());
		return stats;
	}
}
