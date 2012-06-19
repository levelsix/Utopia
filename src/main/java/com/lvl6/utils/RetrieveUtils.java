package com.lvl6.utils;

import com.lvl6.retrieveutils.UserCityRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.spring.AppContext;

public class RetrieveUtils {
	
	public static UserRetrieveUtils userRetrieveUtils() {
		return AppContext.getApplicationContext().getBeansOfType(UserRetrieveUtils.class).get(0);
	}
	
	public static UserCityRetrieveUtils userCityRetrieveUtils() {
		return AppContext.getApplicationContext().getBeansOfType(UserCityRetrieveUtils.class).get(0);
	}
	

	  
	  public static UserEquipRetrieveUtils userEquipRetrieveUtils() {
		  return AppContext.getApplicationContext().getBean(UserEquipRetrieveUtils.class);
	  }
	
}
