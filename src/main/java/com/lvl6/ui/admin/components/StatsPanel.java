package com.lvl6.ui.admin.components;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

public class StatsPanel extends Panel {

	public StatsPanel(String id) {
		super(id);
		addStats();
		
	}
	
	
	
	protected StatsModel stats = new StatsModel();
	protected PropertyModel<Integer> cplayers = new PropertyModel<Integer>(stats, "connectedPlayersCount");
	protected PropertyModel<Integer> tplayers = new PropertyModel<Integer>(stats, "totalPlayersCount");
	protected PropertyModel<Integer> totalPayingPlayersPM = new PropertyModel<Integer>(stats, "totalPayingPlayers");
	protected PropertyModel<Integer> loggedInTodayPM = new PropertyModel<Integer>(stats, "loggedInToday");
	protected PropertyModel<Integer> loggedInThisWeekPM = new PropertyModel<Integer>(stats, "loggedInThisWeek");
	protected PropertyModel<Long> totalInAppPurchasesPM = new PropertyModel<Long>(stats, "totalInAppPurchases");
	protected PropertyModel<Long> sumOfInAppPurchasesPM = new PropertyModel<Long>(stats, "sumOfInAppPurchases");
	protected PropertyModel<Long> afterAppleTaxPM = new PropertyModel<Long>(stats, "afterAppleTax");
	protected PropertyModel<Long> countNumberKiipRewardsRedeemedPM = new PropertyModel<Long>(stats, "countNumberKiipRewardsRedeemed");
	protected PropertyModel<Long> countMarketplaceTransactionsPM = new PropertyModel<Long>(stats, "countMarketplaceTransactions");
	protected PropertyModel<Long> countMarketplacePostsPM = new PropertyModel<Long>(stats, "countMarketplacePosts");
	protected PropertyModel<Long> sumOfSilverInWorldPM = new PropertyModel<Long>(stats, "sumOfSilverInWorld");
	protected PropertyModel<Long> sumOfDiamondsInWorldPM = new PropertyModel<Long>(stats, "sumOfDiamondsInWorld");
	protected PropertyModel<Long> averageSilverPerPlayerPM = new PropertyModel<Long>(stats, "averageSilverPerPlayer");
	protected PropertyModel<Long> percentageOfPaidPlayersPM = new PropertyModel<Long>(stats, "percentageOfPaidPlayers");
	protected PropertyModel<Long> revenuePerUserPM = new PropertyModel<Long>(stats, "revenuePerUser");
	protected PropertyModel<Long> revenuePerPayingUserPM = new PropertyModel<Long>(stats, "revenuePerPayingUser");
	protected PropertyModel<Long> purchasesPerPaidPlayerPM = new PropertyModel<Long>(stats, "purchasesPerPaidPlayer");


	protected Label cplayersLabel = new Label("connectedPlayers", cplayers);
	protected Label tplayersLabel = new Label("totalPlayers", tplayers);
	protected Label totalPayingPlayersLabel = new Label("totalPayingPlayers", totalPayingPlayersPM);
	protected Label loggedInTodayLabel = new Label("loggedInToday", loggedInTodayPM);
	protected Label loggedInThisWeekLabel = new Label("loggedInThisWeek", loggedInThisWeekPM);
	protected Label totalInAppPurchasesLabel = new Label("totalInAppPurchases", totalInAppPurchasesPM);
	protected Label sumOfInAppPurchasesLabel = new Label("sumOfInAppPurchases", sumOfInAppPurchasesPM);
	protected Label afterAppleTaxLabel = new Label("afterAppleTax", afterAppleTaxPM);
	protected Label countNumberKiipRewardsRedeemedLabel = new Label("countNumberKiipRewardsRedeemed", countNumberKiipRewardsRedeemedPM);
	protected Label countMarketplaceTransactionsLabel = new Label("countMarketplaceTransactions", countMarketplaceTransactionsPM);
	protected Label countMarketplacePostsLabel = new Label("countMarketplacePosts", countMarketplacePostsPM);
	protected Label sumOfSilverInWorldLabel = new Label("sumOfSilverInWorld", sumOfSilverInWorldPM);
	protected Label sumOfDiamondsInWorldLabel = new Label("sumOfDiamondsInWorld", sumOfDiamondsInWorldPM);
	protected Label averageSilverPerPlayerLabel = new Label("averageSilverPerPlayer", averageSilverPerPlayerPM);
	protected Label percentageOfPaidPlayersLabel = new Label("percentageOfPaidPlayers", percentageOfPaidPlayersPM);
	protected Label revenuePerUserLabel = new Label("revenuePerUser", revenuePerUserPM);
	protected Label revenuePerPayingUserLabel = new Label("revenuePerPayingUser", revenuePerPayingUserPM);
	protected Label purchasesPerPaidPlayerLabel = new Label("purchasesPerPaidPlayer", purchasesPerPaidPlayerPM);
	
	//"totalPayingPlayers","totalInAppPurchases","sumOfInAppPurchases","countNumberKiipRewardsRedeemed","countMarketplaceTransactions","countMarketplacePosts","sumOfSilverInWorld","sumOfDiamondsInWorld"

	
	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(90))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			target.add(cplayersLabel);
			target.add(tplayersLabel);
			target.add(totalPayingPlayersLabel);
			target.add(totalInAppPurchasesLabel);
			target.add(loggedInTodayLabel);
			target.add(loggedInThisWeekLabel);
			target.add(afterAppleTaxLabel);
			target.add(sumOfInAppPurchasesLabel);
			target.add(countNumberKiipRewardsRedeemedLabel);
			target.add(countMarketplaceTransactionsLabel);
			target.add(countMarketplacePostsLabel);
			target.add(sumOfSilverInWorldLabel);
			target.add(sumOfDiamondsInWorldLabel);
			target.add(averageSilverPerPlayerLabel);
			target.add(revenuePerUserLabel);
			target.add(revenuePerPayingUserLabel);
			target.add(percentageOfPaidPlayersLabel);
			target.add(purchasesPerPaidPlayerLabel);
		}
	};
	
	protected void addStats() {
		loggedInTodayLabel.setOutputMarkupId(true);
		loggedInThisWeekLabel.setOutputMarkupId(true);
		afterAppleTaxLabel.setOutputMarkupId(true);
		cplayersLabel.setOutputMarkupId(true);
		tplayersLabel.setOutputMarkupId(true);
		totalPayingPlayersLabel.setOutputMarkupId(true);
		totalInAppPurchasesLabel.setOutputMarkupId(true);
		sumOfInAppPurchasesLabel.setOutputMarkupId(true);
		countNumberKiipRewardsRedeemedLabel.setOutputMarkupId(true);
		countMarketplaceTransactionsLabel.setOutputMarkupId(true);
		countMarketplacePostsLabel.setOutputMarkupId(true);
		sumOfSilverInWorldLabel.setOutputMarkupId(true);
		sumOfDiamondsInWorldLabel.setOutputMarkupId(true);
		averageSilverPerPlayerLabel.setOutputMarkupId(true);
		revenuePerUserLabel.setOutputMarkupId(true);
		revenuePerPayingUserLabel.setOutputMarkupId(true);
		percentageOfPaidPlayersLabel.setOutputMarkupId(true);
		purchasesPerPaidPlayerLabel.setOutputMarkupId(true);
		add(cplayersLabel);
		add(tplayersLabel);
		add(totalPayingPlayersLabel);
		add(totalInAppPurchasesLabel);
		add(sumOfInAppPurchasesLabel);
		add(loggedInTodayLabel);
		add(loggedInThisWeekLabel);
		add(afterAppleTaxLabel);
		add(countNumberKiipRewardsRedeemedLabel);
		add(countMarketplaceTransactionsLabel);
		add(countMarketplacePostsLabel);
		add(sumOfSilverInWorldLabel);
		add(sumOfDiamondsInWorldLabel);
		add(averageSilverPerPlayerLabel);
		add(abstractAjaxTimerBehavior);
		add(revenuePerUserLabel);
		add(revenuePerPayingUserLabel);
		add(percentageOfPaidPlayersLabel);
		add(purchasesPerPaidPlayerLabel);
	}

	private static final long serialVersionUID = -2625835646085053890L;

}
