package com.lvl6.ui.admin.components;

import org.apache.wicket.model.AbstractReadOnlyModel;

import com.lvl6.spring.AppContext;
import com.lvl6.utils.ApplicationUtils;

public class StatsModel extends AbstractReadOnlyModel<ApplicationStats> {

	private static final long serialVersionUID = 4348674057732427062L;

	@Override
	public ApplicationStats getObject() {
		ApplicationUtils utils = AppContext.getApplicationContext().getBean(ApplicationUtils.class);
		return utils.getStats();
	}

}
