package com.lvl6.test.staticdbdata;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.info.Equipment;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.server.controller.EarnFreeDiamondsController;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class QuestTests {

	
	private static final int MAX_EQUIPMENT_DESCRIPTION_LENGTH = 0;



	@Test
	public void testEquipmentDescriptionLengths(){
		Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
		
		for (Integer equipId : equipmentIdsToEquipment.keySet()) {
			Equipment equip = equipmentIdsToEquipment.get(equipId);
			Assert.assertTrue(equip.getDescription().length() < MAX_EQUIPMENT_DESCRIPTION_LENGTH);
		}
	}
	

}
