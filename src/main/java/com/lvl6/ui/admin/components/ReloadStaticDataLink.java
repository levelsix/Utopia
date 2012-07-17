package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.lvl6.server.ServerMessage;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ReloadStaticDataLink extends Form<String>{
	
	Logger log = LoggerFactory.getLogger(getClass());
	
	
	@Override
	protected void onSubmit() {
		super.onSubmit();
		HazelcastInstance instance = (HazelcastInstance) AppContext.getApplicationContext().getBean("hazel");
		ITopic<ServerMessage> topic = instance.getTopic("serverEvents");
		log.info("Reloading all static data for cluster");
		topic.publish(ServerMessage.RELOAD_STATIC_DATA);
		setResponsePage(AdminPage.class);
	}
	
	

	public ReloadStaticDataLink(String id) {
		super(id);
	}

	private static final long serialVersionUID = -161974445690777238L;


}