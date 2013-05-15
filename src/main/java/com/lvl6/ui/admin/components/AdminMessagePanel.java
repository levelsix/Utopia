package com.lvl6.ui.admin.components;

import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.info.AdminChatPost;
import com.lvl6.properties.ControllerConstants;

public class AdminMessagePanel extends Panel {

	private static final Logger log = LoggerFactory.getLogger(AdminMessagePanel.class);

	protected AdminChatPost adminChatPost;

	public AdminMessagePanel(String id, AdminChatPost acp) {
		super(id);
		this.adminChatPost = acp;
		setup();
	}

	protected void setup() {
		addMessageFrom();
		addMessageTo();
		addMessageContent();
		addReplyLink();
		addMarkAsReadLink();
		addReplyForm();
	}
	
	protected void addMessageContent() {
		Label content = new Label("messageContent", adminChatPost.getContent());
		add(content);
	}

	protected void addMessageFrom() {
		String fromText = adminChatPost.getPosterId() == ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID ? "Admin" : adminChatPost.getUsername();
		Label from = new Label("messageFrom", fromText);
		add(from);
	}

	protected void addMessageTo() {
		String toText = adminChatPost.getPosterId() != ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID ? "Admin" : adminChatPost.getUsername();
		Label to = new Label("messageTo", toText);
		add(to);
	}
	
	AjaxLink<String> replyLink;
	
	protected void addReplyLink() {
		 replyLink = new AjaxLink<String>("replyLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				replyLink.setVisible(false);
				target.add(replyLink);
				form.setVisible(true);
				target.add(form);
			}
		};
		add(replyLink);
		replyLink.setOutputMarkupId(true);
	}

	
	AjaxLink<String> markAsRead;
	
	protected void addMarkAsReadLink() {
		 markAsRead = new AjaxLink<String>("markAsReadLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				
			}
		};
		markAsRead.setVisible(false);
		markAsRead.setOutputMarkupId(true);
		add(markAsRead);
	}
	
	SendAdminMessageForm form;
	
	protected void addReplyForm() {
		form = new SendAdminMessageForm("replyForm",
				new AdminChatPost(
						-1, 
						ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID,
						adminChatPost.getPosterId(), 
						new Date(),
						""));
		form.setOutputMarkupId(true);
		//form.setVisible(false);
		add(form);
	}
	
	
	private static final long serialVersionUID = -2625835646085053890L;

}
