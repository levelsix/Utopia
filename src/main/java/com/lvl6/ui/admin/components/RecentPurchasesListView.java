package com.lvl6.ui.admin.components;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.lvl6.stats.InAppPurchase;

public class RecentPurchasesListView extends ListView<InAppPurchase> {

	public RecentPurchasesListView(String id,
			IModel<? extends List<? extends InAppPurchase>> model) {
		super(id, model);
	}

	private static final long serialVersionUID = 3416205041414287778L;

	@Override
	protected void populateItem(ListItem<InAppPurchase> item) {
		InAppPurchase spend = item.getModelObject();
		item.add(new Label("spender", spend.getUserId().toString()));
    item.add(new Label("name", spend.getName()));
		item.add(new Label("amountSpent", "$"+spend.getCashSpent().longValue()));
		item.add(new Label("purchaseDate", spend.getPurchasedDate().toString()));
	}

}
