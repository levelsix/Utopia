package com.lvl6.ui.admin.components;

import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

public class LogSearchForm extends Form<LogSearchInputModel> {

	public LogSearchForm(String id) {
		super(id, new CompoundPropertyModel<LogSearchInputModel>(new LogSearchInputModel()));
		setup();
	}
	
	
	
	protected void setup() {
		add(new DateTimeField("start"));
		add(new DateTimeField("end"));
		add(new TextField<String>("level"));
		add(new TextField<String>("playerId"));
		add(new TextField<String>("searchInput"));
		add(new TextField<Integer>("offset"));
		add(new TextField<Integer>("show"));
	}
	
	
	
	





	private static final long serialVersionUID = 1L;

}
