package com.lvl6.ui.admin.components;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;

public class TopSpendersPanel extends Panel {

	public TopSpendersPanel(String id) {
		super(id);
		setupPanel();
	}
	
	
	protected TopSpendersModel model = new TopSpendersModel();
	protected TopSpendersListView list = new TopSpendersListView("topSpendersList", model);
	
	
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
