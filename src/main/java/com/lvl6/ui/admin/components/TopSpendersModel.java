package com.lvl6.ui.admin.components;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import com.lvl6.retrieveutils.StatisticsRetrieveUtil;
import com.lvl6.spring.AppContext;
import com.lvl6.stats.Spender;

public class TopSpendersModel extends LoadableDetachableModel<List<Spender>> {

	private static final long serialVersionUID = 7358449148739208537L;
	
	@Override
	protected List<Spender> load() {
		StatisticsRetrieveUtil statsUtil = AppContext.getApplicationContext().getBean(StatisticsRetrieveUtil.class);
		return statsUtil.getTopSpenders(50);
	}

}
