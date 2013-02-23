package com.lvl6.ui.admin.components;

import java.lang.reflect.Field;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.ui.admin.pages.StatsGraphsPage;
import com.lvl6.utils.StringUtils;

public class StatsPanelDynamic extends Panel {

	
	
	private static final Logger log = LoggerFactory.getLogger(StatsPanelDynamic.class);
	
	public static String STATS_FIELD_NAME = "statsField";
	
	public StatsPanelDynamic(String id) {
		super(id);
		addStats();

	}

	
	protected StatsModel statsModel = new StatsModel();
	
	
	protected void addStats() {
		Class<ApplicationStats> stats = ApplicationStats.class;
		RepeatingView rv = new RepeatingView("statsTable");
		for (Field field : stats.getFields()) {
			AbstractItem itm = new AbstractItem(rv.newChildId());
			PageParameters params = new PageParameters();
			params.set(STATS_FIELD_NAME, field.getName());
			//log.info("Adding field {}", field.getName());
			BookmarkablePageLink<StatsGraphsPage> bookmarkablePageLink = new BookmarkablePageLink<StatsGraphsPage>("statGraphLink", StatsGraphsPage.class, params);
			//bookmarkablePageLink.setOutputMarkupId(true);
			itm.add(bookmarkablePageLink);
			Label displayNameLabel = new Label("statDisplayName", StringUtils.displayName(field.getName()));
			bookmarkablePageLink.add(displayNameLabel);
			try {
				Object statValue = field.get(statsModel.getObject());
				String formatted = "";
				if(statValue instanceof Double) {
					formatted = String.format("%.2f", (Double) statValue);
				}else {
					formatted = statValue.toString();
				}
				Label label = new Label("statValue", formatted);
				label.setOutputMarkupId(true);
				itm.add(label);
			} catch (Exception e) {
				log.error("Error reflecting on fields", e);
			}
			itm.setOutputMarkupId(true);
			rv.add(itm);
		}
		add(rv);
	}

	private static final long serialVersionUID = -2625835646085053890L;

	
}
