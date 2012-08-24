package com.lvl6.ui.admin.components;

import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import com.lvl6.cassandra.log4j.Log4jElasticSearchQuery;

public class LogSearchForm extends Form<LogSearchInputModel> {

	public LogSearchForm(String id) {
		super(id, new CompoundPropertyModel<LogSearchInputModel>(new LogSearchInputModel()));
	}
	
	
	
	protected void setup() {
		add(new DateTimeField("start"));
		add(new DateTimeField("end"));
		add(new TextField<String>("level"));
		add(new TextField<String>("searchInput"));
	}
	
	
	
	





	private static final long serialVersionUID = 1L;

}
