package com.lvl6.utils;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;

import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.ui.admin.components.ApplicationStats;

public class ApplicationUtils {

	@Resource(name = "playersByPlayerId")
	protected Map<Integer, ConnectedPlayer> players;
	
	@Resource
	protected StatisticsRetrieveUtil statsUtil;
	
	@Resource
	protected UserRetrieveUtils usersUtil;
	
	

	protected Map<Integer, ConnectedPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(Map<Integer, ConnectedPlayer> players) {
		this.players = players;
	}

	public ApplicationStats getStats() {
		ApplicationStats stats = new ApplicationStats();
		stats.setConnectedPlayersCount(players.size());
		stats.setTotalPlayersCount(usersUtil.countUsers(false));
		stats.setCountMarketplacePosts(statsUtil.countMarketplacePosts());
		stats.setCountMarketplaceTransactions(statsUtil.countMarketplaceTransactions());
		stats.setCountNumberKiipRewardsRedeemed(statsUtil.countNumberKiipRewardsRedeemed());
		stats.setSumOfDiamondsInWorld(statsUtil.sumOfDiamondsInWorld());
		stats.setSumOfInAppPurchases(statsUtil.sumOfInAppPurchases());
		stats.setSumOfSilverInWorld(statsUtil.sumOfSilverInWorld());
		stats.setTotalInAppPurchases(statsUtil.sumOfInAppPurchases());
		stats.setTotalPayingPlayers(statsUtil.countPayingPlayers());
		return stats;
	}
}
