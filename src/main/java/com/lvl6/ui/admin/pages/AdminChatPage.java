package com.lvl6.ui.admin.pages;

import java.util.List;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.info.AdminChatPost;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.AdminMessagePanel;
import com.lvl6.utils.AdminChatUtil;

public class AdminChatPage extends TemplatePage {

	private static final long serialVersionUID = -1728365297134290240L;
	private static Logger log = LoggerFactory.getLogger(AdminChatPage.class);
	
	protected Integer page = 0;
	protected Integer itemsPerPage = 50;
	protected List<AdminChatPost> messages;

	
	protected List<AdminChatPost> getAdminChatMessages(){
		if(messages == null) {
			messages = AppContext.getApplicationContext().getBean(AdminChatUtil.class).getMessagesToAndFromAdmin(page*itemsPerPage, itemsPerPage);
		}
		return messages;
	}
	
	public AdminChatPage() {
		super();
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			String user = SecurityContextHolder.getContext().getAuthentication().getName();
			log.info("Loading Admin Chat Page for: {}", user);
		} else {
			log.info("Loading Admin Chat Page");
		}
		//setupGraphs();
		setupMessages();
		add(abstractAjaxTimerBehavior);
	}
	

	protected void setupMessages() {
		ListView<AdminChatPost> list = new ListView<AdminChatPost>("adminMessages", getAdminChatMessages()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<AdminChatPost> itm) {
				itm.add(new AdminMessagePanel("adminMessage", itm.getModelObject()));
			}
		};
		add(list);
	}
	
	

	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(90))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setResponsePage(AdminChatPage.class);
		}
	};
	

}
