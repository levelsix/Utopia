package com.lvl6.ui.admin.components;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import com.lvl6.server.ApplicationMode;
import com.lvl6.spring.AppContext;

public class MaintenanceModeForm extends Form<ApplicationMode> {

	public MaintenanceModeForm(String id) {
		super(id, new CompoundPropertyModel<ApplicationMode>(AppContext.getApplicationContext().getBean(ApplicationMode.class)));
		setup();
	}
	
	protected void setup() {
		add(new CheckBox("maintenanceMode"));
		add(new TextField<String>("messageForUsers"));
	}
	
	
	
	





	private static final long serialVersionUID = 1L;

}
