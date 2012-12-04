package com.lvl6.ui.admin;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.HealthCheckPage;
import com.lvl6.ui.admin.pages.LogViewerPage;
import com.lvl6.ui.admin.pages.MainPage;
import com.lvl6.ui.admin.pages.StatsGraphsPage;

public class Lvl6Admin extends WebApplication{

	@Override
	protected void init() {
		super.init();
		mountPage("/", MainPage.class);
		mountPage("/admin", AdminPage.class);
		mountPage("/health", HealthCheckPage.class);
		mountPage("/logs", LogViewerPage.class);
		mountPage("/stats/${statsField}", StatsGraphsPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return MainPage.class;
	}



}
