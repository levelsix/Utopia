package com.lvl6.ui.admin.components;

import org.apache.wicket.model.LoadableDetachableModel;

import com.lvl6.spring.AppContext;
import com.lvl6.utils.ApplicationUtils;

public class StatsModel extends LoadableDetachableModel<ApplicationStats> {

	private static final long serialVersionUID = 4348674057732427062L;

	@Override
	protected ApplicationStats load() {
		ApplicationUtils utils = AppContext.getApplicationContext().getBean(ApplicationUtils.class);
		return utils.getStats();
	}

}
