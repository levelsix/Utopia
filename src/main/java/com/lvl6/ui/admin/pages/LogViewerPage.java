package com.lvl6.ui.admin.pages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.Model;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.cassandra.log4j.Log4JConstants;
import com.lvl6.elasticsearch.Log4jElasticSearchQuery;
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
	
	
	
	protected void setup() {
		form.setOutputMarkupId(true);
		resultLabel.setOutputMarkupId(true);
		//feedback.setOutputMarkupId(true);
		add(form);
		add(resultLabel);
		//add(feedback);
	}
	
	
	protected MultiLineLabel resultLabel = new MultiLineLabel("result");
	//final FeedbackPanel feedback = new FeedbackPanel("feedback");
	
	
	protected LogSearchForm form = new LogSearchForm("logSearch") {
		private static final long serialVersionUID = 1L;

		@Override
		protected void onSubmit() {
			super.onSubmit();
			SearchResponse result;
			LogSearchInputModel model = getModelObject();
			Log4jElasticSearchQuery search = AppContext.getApplicationContext().getBean(Log4jElasticSearchQuery.class);
			if(model.getEnd() != null)
				search.setEndDate(model.getEnd());
			if(model.getStart() != null)
				search.setStartDate(model.getStart());
			search.setMessage(model.getSearchInput());
			search.setLevel(model.getLevel());
			if(model.getPlayerId() != null) {
				search.setPlayerId(Integer.valueOf(model.getPlayerId()));
			}
			search.setOffset(model.getOffset());
			search.setLimit(model.getShow());
			result = search.search();
			StringBuilder sb = buildResultString(result);
			//info(sb.toString());
			resultLabel.setDefaultModel(new Model<String>(sb.toString()));
			//add(resultLabel);
		}

		private StringBuilder buildResultString(SearchResponse result) {
			StringBuilder sb = new StringBuilder();
			sb.append("Hits: ")
			.append(result.hits().getTotalHits())
			.append("\n")
			.append("Took: ")
			.append(result.getTookInMillis())
			.append("ms\n")
			.append("Results: \n\n");
			Iterator<SearchHit> it = result.hits().iterator();
			while(it.hasNext()) {
				SearchHit hit = it.next();
				Map<String, Object> hi = hit.sourceAsMap();
				for(String key: hi.keySet()) {
					Object h = hi.get(key);
					sb.append(key)
					.append(": ")
					.append(format(key, h))
					.append("\n");
				}
				sb.append("\n");
			}
			return sb;
		}
		
		protected SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");
		private String format(String key, Object entry) {
			if(key.equals(Log4JConstants.TIME)) {
				return format.format(new Date((Long) entry));
			}
			return entry.toString();
		}
	};

}
