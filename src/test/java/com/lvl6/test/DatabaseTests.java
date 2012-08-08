package com.lvl6.test;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.info.Equipment;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.server.controller.EarnFreeDiamondsController;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.RetrieveUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class DatabaseTests {

	
	private static final int MAX_EQUIPMENT_DESCRIPTION_LENGTH = 0;



//	@Test
	public void testEquipmentDescriptionLengths(){
		Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
		
		for (Integer equipId : equipmentIdsToEquipment.keySet()) {
			Equipment equip = equipmentIdsToEquipment.get(equipId);
			Assert.assertTrue(equip.getDescription().length() < MAX_EQUIPMENT_DESCRIPTION_LENGTH);
		}
	}
	
//  @Test
	public void testGetAllArmoryEquipmentForClassType(){
		List<Equipment> equipmentClassToEquipment = EquipmentRetrieveUtils.getAllArmoryEquipmentForClassType(ClassType.ALL_AMULET);
		Iterator<Equipment> iterator = equipmentClassToEquipment.iterator();
		while(iterator.hasNext()){
			Assert.assertTrue(iterator.next() != null);
		}			
	}
	

}
