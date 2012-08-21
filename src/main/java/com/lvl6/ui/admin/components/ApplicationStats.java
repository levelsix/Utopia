package com.lvl6.ui.admin.components;

public class ApplicationStats {

	protected Integer connectedPlayersCount = 0;
	protected Integer totalPlayersCount = 0;
	protected Integer loggedInToday = 0;
	protected Integer totalPayingPlayers = 0;
	protected Long totalInAppPurchases = 0l;
	protected Long sumOfInAppPurchases = 0l;
	protected Long afterAppleTax = 0l;
	protected Long countNumberKiipRewardsRedeemed = 0l;
	protected Long countMarketplaceTransactions = 0l;
	protected Long countMarketplacePosts = 0l;
	protected Long sumOfSilverInWorld = 0l;
	protected Long sumOfDiamondsInWorld = 0l;

	public Integer getLoggedInToday() {
		return loggedInToday;
	}

	public void setLoggedInToday(Integer loggedInToday) {
		this.loggedInToday = loggedInToday;
	}

	public Long getAfterAppleTax() {
		return afterAppleTax;
	}

	public void setAfterAppleTax(Long afterAppleTax) {
		this.afterAppleTax = afterAppleTax;
	}

	public Integer getPercentageOfPlayersPaying() {
		if (getTotalPlayersCount() == 0) {
			return 0;
		}
		return 100 * (getTotalPayingPlayers() / getTotalPlayersCount());
	}

	public void setPercentageOfPlayersPaying(Integer value) {
	}

	public Long getAverageSilverPerPlayer() {
		if (getTotalPlayersCount() == 0) {
			return 0L;
		}
		return getSumOfSilverInWorld() / getTotalPlayersCount();
	}

	public void setAverageSilverPerPlayer(Long value) {
	}

	public Integer getTotalPayingPlayers() {
		return totalPayingPlayers;
	}

	public void setTotalPayingPlayers(Integer totalPayingPlayers) {
		this.totalPayingPlayers = totalPayingPlayers;
	}

	public Long getTotalInAppPurchases() {
		return totalInAppPurchases;
	}

	public void setTotalInAppPurchases(Long totalInAppPurchases) {
		this.totalInAppPurchases = totalInAppPurchases;
	}

	public Long getSumOfInAppPurchases() {
		return sumOfInAppPurchases;
	}

	public void setSumOfInAppPurchases(Long sumOfInAppPurchases) {
		this.sumOfInAppPurchases = sumOfInAppPurchases;
	}

	public Long getCountNumberKiipRewardsRedeemed() {
		return countNumberKiipRewardsRedeemed;
	}

	public void setCountNumberKiipRewardsRedeemed(
			Long countNumberKiipRewardsRedeemed) {
		this.countNumberKiipRewardsRedeemed = countNumberKiipRewardsRedeemed;
	}

	public Long getCountMarketplaceTransactions() {
		return countMarketplaceTransactions;
	}

	public void setCountMarketplaceTransactions(
			Long countMarketplaceTransactions) {
		this.countMarketplaceTransactions = countMarketplaceTransactions;
	}

	public Long getCountMarketplacePosts() {
		return countMarketplacePosts;
	}

	public void setCountMarketplacePosts(Long countMarketplacePosts) {
		this.countMarketplacePosts = countMarketplacePosts;
	}

	public Long getSumOfSilverInWorld() {
		return sumOfSilverInWorld;
	}

	public void setSumOfSilverInWorld(Long sumOfSilverInWorld) {
		this.sumOfSilverInWorld = sumOfSilverInWorld;
	}

	public Long getSumOfDiamondsInWorld() {
		return sumOfDiamondsInWorld;
	}

	public void setSumOfDiamondsInWorld(Long sumOfDiamondsInWorld) {
		this.sumOfDiamondsInWorld = sumOfDiamondsInWorld;
	}

	public Integer getConnectedPlayersCount() {
		return connectedPlayersCount;
	}

	public void setConnectedPlayersCount(Integer connectedPlayersCount) {
		this.connectedPlayersCount = connectedPlayersCount;
	}

	public Integer getTotalPlayersCount() {
		return totalPlayersCount;
	}

	public void setTotalPlayersCount(Integer totalPlayersCount) {
		this.totalPlayersCount = totalPlayersCount;
	}

}
