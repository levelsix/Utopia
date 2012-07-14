package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.link.Link;

public class ReloadStaticDataLink extends Link<String>{
	public ReloadStaticDataLink(String id) {
		super(id);
	}

	private static final long serialVersionUID = -161974445690777238L;

	@Override
	public void onClick() {
		//do static data reload here
	}

}
