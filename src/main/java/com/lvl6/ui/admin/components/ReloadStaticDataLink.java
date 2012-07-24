package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.link.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.server.ServerAdmin;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.MainPage;

public class ReloadStaticDataLink extends Link<String>{
	
	Logger log = LoggerFactory.getLogger(getClass());
	

	public ReloadStaticDataLink(String id) {
		super(id);
	}

	private static final long serialVersionUID = -161974445690777238L;


	@Override
	public void onClick() {
		log.info("An Admin requested a purge of all static data");
		ServerAdmin sa = AppContext.getApplicationContext().getBean(ServerAdmin.class);
		sa.reloadAllStaticData();
		setResponsePage(MainPage.class);		
	}


}
