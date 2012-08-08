package com.lvl6.test.staticdbdata;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.server.controller.EarnFreeDiamondsController;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class EquipmentTests {

	
	@Test
	public void testAppStart(){
		RetrieveUtils.userCityRetrieveUtils();
		RetrieveUtils.userEquipRetrieveUtils();
		RetrieveUtils.userQuestRetrieveUtils();
		RetrieveUtils.userRetrieveUtils();
	}
	
	
	@Test
	public void testAutowiringAbstractClass(){
		EarnFreeDiamondsController efdc = AppContext.getApplicationContext().getBean(EarnFreeDiamondsController.class);
		Assert.assertTrue(efdc != null);
		Assert.assertTrue(efdc.getTransactionManager() != null);
		Assert.assertTrue(efdc.getTransactionTemplate() != null);
	}
}
