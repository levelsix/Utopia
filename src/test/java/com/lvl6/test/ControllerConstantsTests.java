package com.lvl6.test;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.info.Equipment;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.server.controller.EarnFreeDiamondsController;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class ControllerConstantsTests {
	
	private static final int ONE=1;
	private static final int ZERO=0;
	
	public void testMaxStatGain(){
		Assert.assertEquals(ControllerConstants.USE_SKILL_POINT__MAX_STAT_GAIN, ONE);
	}
	
	public void testDiamondCostToInstaBuildFirstStruct(){
		Assert.assertEquals(ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT, ZERO);
	}
	
	public void testDailyBonusPercentageChance(){
		Assert.assertEquals(ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_COMMON_EQUIP+ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_UNCOMMON_EQUIP+ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_RARE_EQUIP+ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_EPIC_EQUIP+ControllerConstants.STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_LEGENDARY_EQUIP, 1);
	}
	
	public void testFirstTaskID(){
		Assert.assertEquals(ControllerConstants.TUTORIAL__FIRST_TASK_ID, ONE);		
	}
	
	public void testBattleA(){
		Assert.assertTrue(ControllerConstants.BATTLE__A<1);
	}
	
	public void testFakeEquipPercentOfArmoryPriceListing(){
		Assert.assertTrue(ControllerConstants.RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING < 1);
	}
	
	
}
