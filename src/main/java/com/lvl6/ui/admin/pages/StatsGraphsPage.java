package com.lvl6.ui.admin.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.ui.admin.components.StatsGraphsPanel;

public class StatsGraphsPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(StatsGraphsPage.class);

	public StatsGraphsPage() {
		super();
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Stats Page for: {}", user);
		} else {
			log.info("Loading Stats Page");
		}

		setupGraphs();
	}
		
	
	protected void setupGraphs() {
		StatsGraphsPanel statsGraphs = new StatsGraphsPanel("statsGraphs");
		add(statsGraphs);
	}
	
	

}
