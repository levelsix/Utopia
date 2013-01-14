package com.lvl6.ui.admin.pages;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.lvl6.properties.Globals;
import com.lvl6.server.DevOps;
import com.lvl6.server.ServerAdmin;
import com.lvl6.spring.AppContext;
import com.lvl6.ui.admin.components.MaintenanceModeForm;
import com.lvl6.ui.admin.components.RecentPurchasesPanel;
import com.lvl6.ui.admin.components.ReloadLeaderboardLink;
import com.lvl6.ui.admin.components.ReloadStaticDataLink;
import com.lvl6.ui.admin.components.StatsPanelDynamic;
import com.lvl6.ui.admin.components.TopSpendersPanel;
import com.lvl6.utils.MessagingUtil;

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
		setContactAdmins();
		setSendAdminMessage();
		setMaintenanceMode();
		//setupGraphs();
		add(abstractAjaxTimerBehavior);
	}
	
	private void setMaintenanceMode() {
		
	}
	
	protected MaintenanceModeForm maintenanceForm = new MaintenanceModeForm("maintenanceForm") {
		private static final long serialVersionUID = 1L;
		@Override
		protected void onSubmit() {
			super.onSubmit();
			log.info("Setting maintenance mode");
			ServerAdmin admin = AppContext.getApplicationContext().getBean(ServerAdmin.class);
			admin.setAppMode(getModelObject());
		}
	};
	
	private void setIsSandbox() {
		add(new Label("isSandbox", "Sandbox: "+Globals.IS_SANDBOX()));
	}
	
	
	private void setTools() {
		add(new ReloadStaticDataLink("reloadStaticDataLink"));
		add(new ReloadLeaderboardLink("reloadLeaderboardLink"));
	}

	protected void setStats() {
		add(new StatsPanelDynamic("statsPanel"));
	}
	
	protected void setTopSpenders() {
		add(new TopSpendersPanel("topSpenders"));
	}
	
	protected void setRecentPurchases(){
		add(new RecentPurchasesPanel("recentPurchases"));
	}
	
	
	protected void setContactAdmins() {
		final TextField<String> message = new TextField<String>("message", new Model<String>());
		Form<String> contact = new Form<String>("contactAdmins") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit() {
				super.onSubmit();
				DevOps devops = AppContext.getApplicationContext().getBean(DevOps.class);
				devops.sendAlertToAdmins(message.getModelObject());
				message.setModelObject("Message sent");
			}
		};
		contact.add(message);
		contact.setOutputMarkupId(true);
		add(contact);
	}
	
	
	

	protected void setSendAdminMessage() {
		final TextField<String> message = new TextField<String>("adminMessageToClients", new Model<String>());
		Form<String> contact = new Form<String>("adminMessageForm") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit() {
				super.onSubmit();
				MessagingUtil util = AppContext.getApplicationContext().getBean(MessagingUtil.class);
				util.sendAdminMessage(message.getModelObject());
				message.setModelObject("Admin message sent");
			}
		};
		contact.add(message);
		contact.setOutputMarkupId(true);
		add(contact);
	}
	
	
	
	protected void setupGraphs() {
		BookmarkablePageLink<StatsGraphsPage> link = new BookmarkablePageLink<StatsGraphsPage>("statsGraphs", StatsGraphsPage.class);
		add(link);
	}
	

	AbstractAjaxTimerBehavior abstractAjaxTimerBehavior = new AbstractAjaxTimerBehavior(Duration.seconds(90))
	{
		private static final long serialVersionUID = 5721917435743521271L;

		@Override
		protected void onTimer(AjaxRequestTarget target)
		{
			setResponsePage(AdminPage.class);
		}
	};
	

}
