package com.lvl6.ui.admin.components;

import java.util.List;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateTime;

import com.googlecode.wicketcharts.highcharts.HighChartContainer;
import com.lvl6.cassandra.RollupEntry;
import com.lvl6.cassandra.RollupUtil;
import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;

public class StatsGraphsPanel extends Panel {

	public StatsGraphsPanel(String id) {
		super(id);
		setupPanel();
	}
	
		
	protected void  setupPanel() {
		RollupUtil rolo = AppContext.getApplicationContext().getBean(RollupUtil.class);
		List<RollupEntry> totalPlayers = rolo.findEntries("totalPlayersCount:daily", new DateTime().minusMonths(1).getMillis(), System.currentTimeMillis());
		TimeSeriesLineChartOptions opts = new TimeSeriesLineChartOptions("Total Players", "Players", totalPlayers);
		add(new HighChartContainer("totalPlayers", opts));
		//add();
	}
	
/*	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(300))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setupPanel();
		}
	};*/


	private static final long serialVersionUID = -2625835646085053890L;

}
