package com.lvl6.ui.admin.pages;


import java.util.Map;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.ReloadStaticDataLink;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.utils.ConnectedPlayer;

public class AdminPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(AdminPage.class);

	public AdminPage() {
		super();
		String user = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Loading Admin Page for: "+user);
		add(new ReloadStaticDataLink("reloadStaticDataLink"));
		setStats();
	}

	protected void setStats() {
		add(new StatsPanel("statsPanel"));
	}
	
	
	

}
