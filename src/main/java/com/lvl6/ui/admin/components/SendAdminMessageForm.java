package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import com.lvl6.info.AdminChatPost;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.AdminChatUtil;

public class SendAdminMessageForm extends Form<AdminChatPost> {

	public SendAdminMessageForm(String id, AdminChatPost message) {
		super(id, new CompoundPropertyModel<AdminChatPost>(message));
		setup();
	}
	
	protected void setup() {
		TextField<String> content =new TextField<String>("content");
		content.setOutputMarkupId(true);
		add(content);
	}


	@Override
	protected void onSubmit() {
		super.onSubmit();
		AdminChatUtil acu = AppContext.getApplicationContext().getBean(AdminChatUtil.class);
		acu.sendAdminChatMessage(getModelObject());
	}

	private static final long serialVersionUID = 1L;

}
