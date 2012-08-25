package com.lvl6.ui.admin.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.elasticsearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.cassandra.log4j.Log4jElasticSearchQuery;
import com.lvl6.properties.Globals;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.LogSearchForm;
import com.lvl6.ui.admin.components.LogSearchInputModel;
import com.lvl6.ui.admin.components.RecentPurchasesPanel;
import com.lvl6.ui.admin.components.ReloadStaticDataLink;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.ui.admin.components.TopSpendersPanel;

public class LogViewerPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(LogViewerPage.class);

	public LogViewerPage() {
		super();
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Admin Page for: {}", user);
		} else {
			log.info("Loading Admin Page");
		}

	}
	
	protected SearchResponse result;
	
	
	protected void setup() {
		form.setOutputMarkupId(true);
		resultLabel.setOutputMarkupId(true);
		add(form);
		add(resultLabel);
		add(feedback);
	}
	
	
	protected Label resultLabel = new Label("result");
	final FeedbackPanel feedback = new FeedbackPanel("feedback");
	
	protected LogSearchForm form = new LogSearchForm("logSearch") {
		private static final long serialVersionUID = 1L;

		@Override
		protected void onSubmit() {
			// TODO Auto-generated method stub
			super.onSubmit();
			LogSearchInputModel model = getModelObject();
			Log4jElasticSearchQuery search = AppContext.getApplicationContext().getBean(Log4jElasticSearchQuery.class);
			search.setEndDate(model.getEnd());
			search.setStartDate(model.getStart());
			search.setMessage(model.getSearch());
			search.setLevel(model.getLevel());
			result = search.search();
			info(result.toString());
		}
	};

}
