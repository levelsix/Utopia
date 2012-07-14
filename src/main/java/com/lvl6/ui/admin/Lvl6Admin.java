package com.lvl6.ui.admin;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

import com.lvl6.ui.admin.pages.MainPage;

public class Lvl6Admin extends WebApplication{

	@Override
	protected void init() {
		super.init();
		mountPage("/", MainPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return MainPage.class;
	}



}
