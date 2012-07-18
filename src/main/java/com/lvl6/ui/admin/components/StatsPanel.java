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
	protected Label cplayersLabel = new Label("connectedPlayers", cplayers);
	protected Label tplayersLabel = new Label("totalPlayers", tplayers);

	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(1))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			target.add(cplayersLabel);
			target.add(tplayersLabel);
		}
	};
	
	protected void addStats() {
		cplayersLabel.setOutputMarkupId(true);
		tplayersLabel.setOutputMarkupId(true);
		add(cplayersLabel);
		add(tplayersLabel);
		add(abstractAjaxTimerBehavior);
	}

	private static final long serialVersionUID = -2625835646085053890L;

}
