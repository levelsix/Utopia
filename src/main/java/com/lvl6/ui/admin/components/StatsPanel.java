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
	protected PropertyModel<Long> totalInAppPurchasesPM = new PropertyModel<Long>(stats, "totalInAppPurchases");
	protected PropertyModel<Long> sumOfInAppPurchasesPM = new PropertyModel<Long>(stats, "sumOfInAppPurchases");
	protected PropertyModel<Long> countNumberKiipRewardsRedeemedPM = new PropertyModel<Long>(stats, "countNumberKiipRewardsRedeemed");
	protected PropertyModel<Long> countMarketplaceTransactionsPM = new PropertyModel<Long>(stats, "countMarketplaceTransactions");
	protected PropertyModel<Long> countMarketplacePostsPM = new PropertyModel<Long>(stats, "countMarketplacePosts");
	protected PropertyModel<Long> sumOfSilverInWorldPM = new PropertyModel<Long>(stats, "sumOfSilverInWorld");
	protected PropertyModel<Long> sumOfDiamondsInWorldPM = new PropertyModel<Long>(stats, "sumOfDiamondsInWorld");
	protected PropertyModel<Long> averageSilverPerPlayerPM = new PropertyModel<Long>(stats, "averageSilverPerPlayer");
	protected PropertyModel<Long> percentageOfPlayersPayingPM = new PropertyModel<Long>(stats, "percentageOfPlayersPaying");
	
	
	protected Label cplayersLabel = new Label("connectedPlayers", cplayers);
	protected Label tplayersLabel = new Label("totalPlayers", tplayers);
	protected Label totalPayingPlayersLabel = new Label("totalPayingPlayers", totalPayingPlayersPM);
	protected Label totalInAppPurchasesLabel = new Label("totalInAppPurchases", totalInAppPurchasesPM);
	protected Label sumOfInAppPurchasesLabel = new Label("sumOfInAppPurchases", sumOfInAppPurchasesPM);
	protected Label countNumberKiipRewardsRedeemedLabel = new Label("countNumberKiipRewardsRedeemed", countNumberKiipRewardsRedeemedPM);
	protected Label countMarketplaceTransactionsLabel = new Label("countMarketplaceTransactions", countMarketplaceTransactionsPM);
	protected Label countMarketplacePostsLabel = new Label("countMarketplacePosts", countMarketplacePostsPM);
	protected Label sumOfSilverInWorldLabel = new Label("sumOfSilverInWorld", sumOfSilverInWorldPM);
	protected Label sumOfDiamondsInWorldLabel = new Label("sumOfDiamondsInWorld", sumOfDiamondsInWorldPM);
	protected Label averageSilverPerPlayerLabel = new Label("averageSilverPerPlayer", averageSilverPerPlayerPM);
	protected Label percentageOfPlayersPayingLabel = new Label("percentageOfPlayersPaying", percentageOfPlayersPayingPM);
	
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
			target.add(sumOfInAppPurchasesLabel);
			target.add(countNumberKiipRewardsRedeemedLabel);
			target.add(countMarketplaceTransactionsLabel);
			target.add(countMarketplacePostsLabel);
			target.add(sumOfSilverInWorldLabel);
			target.add(sumOfDiamondsInWorldLabel);
			//target.add(averageSilverPerPlayerLabel);
			target.add(percentageOfPlayersPayingLabel);
		}
	};
	
	protected void addStats() {
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
		percentageOfPlayersPayingLabel.setOutputMarkupId(true);
		add(cplayersLabel);
		add(tplayersLabel);
		add(totalPayingPlayersLabel);
		add(totalInAppPurchasesLabel);
		add(sumOfInAppPurchasesLabel);
		add(countNumberKiipRewardsRedeemedLabel);
		add(countMarketplaceTransactionsLabel);
		add(countMarketplacePostsLabel);
		add(sumOfSilverInWorldLabel);
		add(sumOfDiamondsInWorldLabel);
		//add(averageSilverPerPlayerLabel);
		add(percentageOfPlayersPayingLabel);
		add(abstractAjaxTimerBehavior);
	}

	private static final long serialVersionUID = -2625835646085053890L;

}
