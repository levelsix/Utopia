package com.lvl6.ui.admin.pages;


import com.lvl6.ui.admin.components.ReloadStaticDataLink;

public class AdminPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;

	public AdminPage() {
		super();
		add(new ReloadStaticDataLink("reloadStaticDataLink"));
	}
	
	
	

}
