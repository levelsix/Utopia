package com.lvl6.ui.admin.components;

import java.lang.reflect.Field;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.ui.admin.pages.AdminPage;
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
			log.info("Adding field {}", field.getName());
			BookmarkablePageLink<StatsGraphsPage> bookmarkablePageLink = new BookmarkablePageLink<StatsGraphsPage>("statGraphLink", StatsGraphsPage.class, params);
			//bookmarkablePageLink.setOutputMarkupId(true);
			itm.add(bookmarkablePageLink);
			itm.add(new Label("statDisplayName", StringUtils.displayName(field.getName())));
			try {
				Label label = new Label("statValue", field.get(statsModel.getObject()).toString());
				label.setOutputMarkupId(true);
				itm.add(label);
			} catch (Exception e) {
				log.error("Error reflecting on fields", e);
			}
			itm.setOutputMarkupId(true);
		}
		add(rv);
	}

	private static final long serialVersionUID = -2625835646085053890L;

	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(90))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setResponsePage(AdminPage.class);
		}
	};
	
}
