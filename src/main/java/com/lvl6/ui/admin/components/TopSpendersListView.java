package com.lvl6.ui.admin.components;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.lvl6.stats.Spender;

public class TopSpendersListView extends ListView<Spender> {

	public TopSpendersListView(String id,
			IModel<? extends List<? extends Spender>> model) {
		super(id, model);
	}

	private static final long serialVersionUID = 3416205041414287778L;

	@Override
	protected void populateItem(ListItem<Spender> item) {
		Spender spend = item.getModelObject();
    item.add(new Label("spender", spend.getUserId().toString()));
    item.add(new Label("name", spend.getName()));
		item.add(new Label("amountSpent", "$"+spend.getAmountSpent().longValue()));
		item.add(new Label("userName", spend.getUserName()));
		
	}

}
