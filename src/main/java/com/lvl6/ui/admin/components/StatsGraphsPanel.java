package com.lvl6.ui.admin.components;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.wicketcharts.highcharts.HighChartContainer;
import com.lvl6.cassandra.RollupEntry;
import com.lvl6.cassandra.RollupUtil;
import com.lvl6.spring.AppContext;

public class StatsGraphsPanel extends Panel {

	private static Logger log = LoggerFactory.getLogger(StatsGraphsPanel.class);
	
	public StatsGraphsPanel(String id) {
		super(id);
		setupPanel();
	}
	
	
		
	protected void  setupPanel() {
		List<List<RollupEntry>> graphs = getGraphs();
		RepeatingView view = new RepeatingView("graphs");
		for(List<RollupEntry> graph : graphs) {
			AbstractItem itm = new AbstractItem(view.newChildId());
			view.add(itm);
			TimeSeriesLineChartOptions opts = new TimeSeriesLineChartOptions(graph.get(0).getKey(), "Values", graph);
			itm.add(new HighChartContainer("aGraph", opts));
			
		}
		add(view);
	}
	
	protected List<List<RollupEntry>> getGraphs(){
		RollupUtil rolo = AppContext.getApplicationContext().getBean(RollupUtil.class);
		List<List<RollupEntry>> graphs = new ArrayList<List<RollupEntry>>();
		Class<ApplicationStats> statsClass = ApplicationStats.class;
		Field[] fieldList = statsClass.getDeclaredFields();
		long twoWeeksAgo = new DateTime().minusDays(14).getMillis();
		long now = System.currentTimeMillis();
		for(int i = 0; i < fieldList.length; i++) {
			Field field = fieldList[i];
			String key = field.getName()+":hour";
			List<RollupEntry> entries = rolo.findEntries(key, twoWeeksAgo, now);
			if(entries != null && !entries.isEmpty()) {
				log.info("Adding {} size: {}", key, entries.size());
				graphs.add(entries);
			}
		}
		return graphs;
	}
	

	private static final long serialVersionUID = -2625835646085053890L;

}
