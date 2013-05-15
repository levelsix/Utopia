package com.lvl6.utils;


import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hazelcast.core.IMap;
import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.ui.admin.components.ApplicationStats;

@Component
public class ApplicationUtils {

	private static Logger log = LoggerFactory.getLogger(ApplicationUtils.class);
	
	@Resource(name = "playersByPlayerId")
	protected IMap<Integer, ConnectedPlayer> players;
	
	@Resource
	protected StatisticsRetrieveUtil statsUtil;
	
	@Resource
	protected UserRetrieveUtils usersUtil;
	
	

	protected IMap<Integer, ConnectedPlayer> getPlayers() {
		return players;
	}

	public void setPlayers(IMap<Integer, ConnectedPlayer> players) {
		this.players = players;
	}

	public ApplicationStats getStats() {
		log.debug("Getting application stats");
		ApplicationStats stats = new ApplicationStats();
		stats.setConnectedPlayersCount(players.size());
		stats.setTotalPlayersCount(usersUtil.countUsers(false));
		stats.setLoggedInToday(statsUtil.countLoginsToday());
		stats.setLoggedInThisWeek(statsUtil.countLoginsThisWeek());
		stats.setCountMarketplacePosts(statsUtil.countMarketplacePosts());
		stats.setCountMarketplaceTransactions(statsUtil.countMarketplaceTransactions());
		stats.setCountNumberKiipRewardsRedeemed(statsUtil.countNumberKiipRewardsRedeemed());
		stats.setSumOfDiamondsInWorld(statsUtil.sumOfDiamondsInWorld());
		stats.setSumOfInAppPurchases(statsUtil.sumOfInAppPurchases());
		Double appleTx = stats.getSumOfInAppPurchases()*.7;
		stats.setAfterAppleTax(appleTx.longValue());
		stats.setSumOfSilverInWorld(statsUtil.sumOfSilverInWorld());
		stats.setTotalInAppPurchases(statsUtil.countInAppPurchases());
		stats.setTotalPayingPlayers(statsUtil.countPayingPlayers());
		return stats;
	}
}
