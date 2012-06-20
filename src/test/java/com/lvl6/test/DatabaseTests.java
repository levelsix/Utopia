package com.lvl6.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.utils.RetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class DatabaseTests {

	
	@Test
	public void testAppStart(){
		RetrieveUtils.userCityRetrieveUtils();
		RetrieveUtils.userEquipRetrieveUtils();
		RetrieveUtils.userQuestRetrieveUtils();
		RetrieveUtils.userRetrieveUtils();
	}
}
