package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.utils.utilmethods.MiscMethods;

public class ReloadStaticDataLink extends Form<String>{
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	
	@Override
	protected void onSubmit() {
		super.onSubmit();
		log.info("Reloading all static data");
		MiscMethods.reloadAllRareChangeStaticData();
	}

	public ReloadStaticDataLink(String id) {
		super(id);
	}

	private static final long serialVersionUID = -161974445690777238L;


}
