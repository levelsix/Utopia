package com.lvl6.ui.admin.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.properties.Globals;
import com.lvl6.ui.admin.components.RecentPurchasesPanel;
import com.lvl6.ui.admin.components.ReloadStaticDataLink;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.ui.admin.components.TopSpendersPanel;

public class AdminPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(AdminPage.class);

	public AdminPage() {
		super();
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Admin Page for: {}", user);
		} else {
			log.info("Loading Admin Page");
		}
		setIsSandbox();
		setTools();
		setStats();
		setTopSpenders();
		setRecentPurchases();
	}
	
	private void setIsSandbox() {
		add(new Label("isSandbox", ""+Globals.IS_SANDBOX()));
	}
	
	
	private void setTools() {
		add(new ReloadStaticDataLink("reloadStaticDataLink"));
	}

	protected void setStats() {
		add(new StatsPanel("statsPanel"));
	}
	
	protected void setTopSpenders() {
		add(new TopSpendersPanel("topSpenders"));
	}
	
	protected void setRecentPurchases(){
		add(new RecentPurchasesPanel("recentPurchases"));
	}

}
