package com.lvl6.ui.admin.pages;

import java.util.Iterator;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.cassandra.log4j.Log4jElasticSearchQuery;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.LogSearchForm;
import com.lvl6.ui.admin.components.LogSearchInputModel;

public class LogViewerPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(LogViewerPage.class);

	public LogViewerPage() {
		super();
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Log Viewer Page for: {}", user);
		} else {
			log.info("Loading Log Viewer Page");
		}
		setup();
	}
	
	protected SearchResponse result;
	
	
	protected void setup() {
		form.setOutputMarkupId(true);
		resultLabel.setOutputMarkupId(true);
		feedback.setOutputMarkupId(true);
		add(form);
		add(resultLabel);
		add(feedback);
	}
	
	
	protected MultiLineLabel resultLabel = new MultiLineLabel("result");
	final FeedbackPanel feedback = new FeedbackPanel("feedback");
	
	
	protected LogSearchForm form = new LogSearchForm("logSearch") {
		private static final long serialVersionUID = 1L;

		@Override
		protected void onSubmit() {
			super.onSubmit();
			LogSearchInputModel model = getModelObject();
			Log4jElasticSearchQuery search = AppContext.getApplicationContext().getBean(Log4jElasticSearchQuery.class);
			search.setEndDate(model.getEnd());
			search.setStartDate(model.getStart());
			search.setMessage(model.getSearchInput());
			search.setLevel(model.getLevel());
			result = search.search();
			StringBuilder sb = new StringBuilder();
			sb.append("Hits: ")
			.append(result.hits().getTotalHits())
			.append("<br />")
			.append("Took: ")
			.append(result.getTookInMillis())
			.append("ms<br />")
			.append("Results: <br />");
			Iterator<SearchHit> it = result.hits().iterator();
			while(it.hasNext()) {
				SearchHit hit = it.next();
				sb.append(hit.getSourceAsString())
				.append("<br />");
			}
			//info(sb.toString());
			resultLabel.setDefaultModel(new Model<String>(sb.toString()));
			//add(resultLabel);
		}
	};

}
