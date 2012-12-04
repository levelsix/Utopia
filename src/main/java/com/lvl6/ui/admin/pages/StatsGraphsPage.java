package com.lvl6.ui.admin.pages;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.ui.admin.components.StatsGraphsPanel;

public class StatsGraphsPage extends TemplatePage {

	public static String STATS_FIELD_ARG = "statsField";
	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(StatsGraphsPage.class);

	public StatsGraphsPage(PageParameters params) {
		super(params);
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Stats Page for: {}", user);
		} else {
			log.info("Loading Stats Page");
		}
		String statsField = params.get(STATS_FIELD_ARG).toString();
		setupGraphs(statsField);
		
	}
		
	
	protected void setupGraphs(String statsField) {
		StatsGraphsPanel statsGraphs = new StatsGraphsPanel("statsGraphs", statsField);
		add(statsGraphs);
	}
	
	

}
