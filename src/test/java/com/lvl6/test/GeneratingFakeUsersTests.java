package com.lvl6.test;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.scriptsjava.generatefakeusers.GenerateFakeUsersWithoutInput;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")

public class GeneratingFakeUsersTests {
	private static Logger log = Logger.getLogger(GenerateFakeUsersWithoutInput.class);
	
	@Test
	public void emptyTest() {
		
	}
	
	//@Test
	public void testGenerateFakeUsers(){
		for (int i = 2; i <= 30; i++){
	        for (int j = 0; j < 10; j++) {
	        	Random random = new Random();
	        	int level = i;
	        	String name = "new player";
	        	UserType type = UserType.valueOf(random.nextInt(UserType.values().length));
	        	
	        	//////////copy below this line///////////////////////////////

	        	int[] fakePlayerStats = GenerateFakeUsersWithoutInput.initializeFakePlayerStats(type, level);
	            int attack = fakePlayerStats[0];
	            int defense = fakePlayerStats[1];
	            int health = fakePlayerStats[2];
	            int weaponEquipped = fakePlayerStats[3];
	            int armorEquipped = fakePlayerStats[4];
	            int amuletEquipped = fakePlayerStats[5];
	            
	            ///////////////////copy above this line///////////////////////////////
	            log.info("Created fake player" + name + " , " + type +  ", with level " +  level + ", attack " + attack  + ", defense "
	            		+ defense + ", health " + health + ", weapon item " + weaponEquipped + ", armor item " + armorEquipped
	            		+ " and amulet item " + amuletEquipped);
	        }
		}
	}
}

