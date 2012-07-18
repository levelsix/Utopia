package com.lvl6.ui.admin.pages;


import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.ReloadStaticDataLink;
import com.lvl6.ui.admin.components.StatsPanel;
import com.lvl6.utils.ConnectedPlayer;

public class AdminPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;

	public AdminPage() {
		super();
		add(new ReloadStaticDataLink("reloadStaticDataLink"));
		setStats();
	}

	protected void setStats() {
		add(new StatsPanel("statsPanel"));
	}
	
	
	

}
