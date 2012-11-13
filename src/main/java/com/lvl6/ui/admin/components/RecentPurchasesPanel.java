package com.lvl6.ui.admin.components;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;

public class RecentPurchasesPanel extends Panel {

	public RecentPurchasesPanel(String id) {
		super(id);
		setupPanel();
	}
	
	
	protected RecentPurchasesModel model = new RecentPurchasesModel();
	protected RecentPurchasesListView list = new RecentPurchasesListView("recentPurchasesList", model);
	
	
	protected void  setupPanel() {
		add(list);
	}
	
	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(300))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setupPanel();
		}
	};


	private static final long serialVersionUID = -2625835646085053890L;

}
