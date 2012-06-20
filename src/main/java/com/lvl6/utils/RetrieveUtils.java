package com.lvl6.utils;

import com.lvl6.retrieveutils.UserCityRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedDefeatTypeJobsRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedTasksRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.spring.AppContext;

public class RetrieveUtils {

	public static UserRetrieveUtils userRetrieveUtils() {
		return AppContext.getApplicationContext()
				.getBean(UserRetrieveUtils.class);
	}

	public static UserCityRetrieveUtils userCityRetrieveUtils() {
		return AppContext.getApplicationContext()
				.getBean(UserCityRetrieveUtils.class);
	}

	public static UserEquipRetrieveUtils userEquipRetrieveUtils() {
		return AppContext.getApplicationContext().getBean(
				UserEquipRetrieveUtils.class);
	}
	
	public static UserQuestRetrieveUtils userQuestRetrieveUtils() {
		return AppContext.getApplicationContext().getBean(
				UserQuestRetrieveUtils.class);
	}

	public static UserStructRetrieveUtils userStructRetrieveUtils() {
		return AppContext.getApplicationContext().getBean(
				UserStructRetrieveUtils.class);
	}

	public static UserQuestsCompletedDefeatTypeJobsRetrieveUtils userQuestsCompletedDefeatTypeJobsRetrieveUtils() {
		return AppContext.getApplicationContext().getBean(
				UserQuestsCompletedDefeatTypeJobsRetrieveUtils.class);
	}
	
	public static UserQuestsCompletedTasksRetrieveUtils userQuestsCompletedTasksRetrieveUtils() {
		return AppContext.getApplicationContext().getBean(
				UserQuestsCompletedTasksRetrieveUtils.class);
	}
}
