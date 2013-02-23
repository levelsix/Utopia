package com.lvl6.ui.admin.components;

public class ApplicationStats {

	public Integer connectedPlayersCount = 0;
	public Integer totalPlayersCount = 0;
	public Integer loggedInToday = 0;
	public Integer loggedInThisWeek = 0;
	public Integer totalPayingPlayers = 0;
	public Long totalInAppPurchases = 0l;
	public Long sumOfInAppPurchases = 0l;
	public Long afterAppleTax = 0l;
	public Double revenuePerPlayer = 0d;
	public Double revenuePerPayingPlayer = 0d;
	public Double percentageOfPaidPlayers = 0d;
	public Double purchasesPerPaidPlayer = 0d;
	public Long countNumberKiipRewardsRedeemed = 0l;
	public Long countMarketplaceTransactions = 0l;
	public Long countMarketplacePosts = 0l;
	public Long sumOfSilverInWorld = 0l;
	public Long sumOfDiamondsInWorld = 0l;

	public Integer getLoggedInThisWeek() {
		return loggedInThisWeek;
	}

	public void setLoggedInThisWeek(Integer loggedInThisWeek) {
		this.loggedInThisWeek = loggedInThisWeek;
	}

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

	protected void setRevenuePerPlayer() {
		if (getSumOfInAppPurchases() > 0 && getTotalPayingPlayers() > 0) {
			revenuePerPayingPlayer = getSumOfInAppPurchases().doubleValue()
					/ getTotalPlayersCount().doubleValue();
		}
	}

	protected void setPurchasesPerPaidPlayer() {
		if (getTotalInAppPurchases() > 0 && getTotalPayingPlayers() > 0) {
			purchasesPerPaidPlayer = getTotalInAppPurchases().doubleValue()
					/ getTotalPayingPlayers().doubleValue();
		}
	}

	protected void setRevenuePerPayingPlayer() {
		if (getSumOfInAppPurchases() > 0 && getTotalPlayersCount() > 0) {
			revenuePerPlayer = getSumOfInAppPurchases().doubleValue() / getTotalPayingPlayers().doubleValue();
		}
	}

	protected void setPercentagePaidPlayers() {
		if (getTotalPayingPlayers() > 0 && getTotalPlayersCount() > 0) {
			percentageOfPaidPlayers = getTotalPayingPlayers().doubleValue()
					/ getTotalPlayersCount().doubleValue();
		}
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
		setRevenuePerPayingPlayer();
		setPercentagePaidPlayers();
		setPurchasesPerPaidPlayer();
	}

	public Long getTotalInAppPurchases() {
		return totalInAppPurchases;

	}

	public void setTotalInAppPurchases(Long totalInAppPurchases) {
		this.totalInAppPurchases = totalInAppPurchases;
		setPurchasesPerPaidPlayer();
	}

	public Long getSumOfInAppPurchases() {
		return sumOfInAppPurchases;
	}

	public void setSumOfInAppPurchases(Long sumOfInAppPurchases) {
		this.sumOfInAppPurchases = sumOfInAppPurchases;
		setRevenuePerPayingPlayer();
		setRevenuePerPlayer();
	}

	public Long getCountNumberKiipRewardsRedeemed() {
		return countNumberKiipRewardsRedeemed;
	}

	public void setCountNumberKiipRewardsRedeemed(Long countNumberKiipRewardsRedeemed) {
		this.countNumberKiipRewardsRedeemed = countNumberKiipRewardsRedeemed;
	}

	public Long getCountMarketplaceTransactions() {
		return countMarketplaceTransactions;
	}

	public void setCountMarketplaceTransactions(Long countMarketplaceTransactions) {
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
		setRevenuePerPlayer();
		setRevenuePerPayingPlayer();
		setPercentagePaidPlayers();
	}

	public Double getRevenuePerUser() {
		return revenuePerPlayer;
	}

	public Double getRevenuePerPayingUser() {
		return revenuePerPayingPlayer;
	}

	public Double getPercentageOfPaidPlayers() {
		return percentageOfPaidPlayers;
	}

	public Double getPurchasesPerPaidPlayer() {
		return purchasesPerPaidPlayer;
	}

}
