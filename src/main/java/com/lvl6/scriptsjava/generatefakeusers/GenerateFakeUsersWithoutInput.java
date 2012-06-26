package com.lvl6.scriptsjava.generatefakeusers;

import java.io.IOException;
import java.util.Random;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lvl6.info.Location;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GenerateFakeUsersWithoutInput {

  private static String nameRulesFile = "src/com/lvl6/scriptsjava/generatefakeusers/namerulesElven.txt";
  private static int numEnemiesToCreatePerLevel = 75;
  private static int minLevel = 2;
  private static int maxLevel = ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER;
  private static int syllablesInName1 = 2;
  private static int syllablesInName2 = 3;

  protected InsertUtil insertUtils;

  public static void main(String[] args) {
	  ApplicationContext context =
		    new ClassPathXmlApplicationContext(new String[] {"test-spring-application-context.xml"});
    NameGenerator nameGenerator = null;
    Random random = new Random();
    try {
      nameGenerator = new NameGenerator(nameRulesFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (nameGenerator != null) {
      DBConnection.get().init();
      for (int i = minLevel; i <= maxLevel; i++){
        for (int j = 0; j < numEnemiesToCreatePerLevel; j++) {
          createUser(random, nameGenerator, i);
        }
      }
      System.out.println("successfully created users!");
    }
  }


  //DeviceTokens are null
  //udid = referral code repeated twice
  private static void createUser(Random random, NameGenerator nameGenerator, int level) {

    int syllablesInName = (Math.random() < .5) ? syllablesInName1 : syllablesInName2;
    String name = nameGenerator.compose(syllablesInName);
    if (Math.random() < .5) name = name.toLowerCase();
    if (Math.random() < .3) name = name + (int)(Math.ceil(Math.random() * 98));

    UserType type = UserType.valueOf(random.nextInt(UserType.values().length));

    Location location = MiscMethods.getRandomValidLocation();

    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.get().deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    } else {
      //TODO: generate more codes?
    }
    
    int[] fakePlayerStats = initializeFakePlayerStats(type, level);
    int attack = fakePlayerStats[0];
    int defense = fakePlayerStats[1];
    int health = fakePlayerStats[2];
    int weaponEquipped = fakePlayerStats[3];
    int armorEquipped = fakePlayerStats[4];
    int amuletEquipped = fakePlayerStats[5];
    
    InsertUtil insertUtils = (InsertUtils) AppContext.getApplicationContext().getBean("insertUtils");

    if (insertUtils.insertUser(newReferCode + newReferCode, name, type, location, null, newReferCode, level, 
        attack, defense, 0, health, 0, 0, 0, 0, weaponEquipped, armorEquipped, amuletEquipped, true) < 0) {
      System.out.println("error in creating user");
    }
  }
  
  public static int[] initializeFakePlayerStats(UserType type, int level) {
	  int attack = 0;
	    int defense = 0;
	    int health = ControllerConstants.TUTORIAL__INIT_HEALTH;
	    int equipmentLevel = level;
	    double characterMultiplier = ControllerConstants.CHARACTERS_ATTACK_DEFENSE_VARIABILITY;
	    int[] initialized = new int[6];
	    
	    health += (int) ((level-1)*ControllerConstants.LEVEL_UP_HEALTH_GAINED);

	    if (equipmentLevel > ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER) {
	    	equipmentLevel = ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER;
	    }
	    
	    Integer weaponEquipped = null, armorEquipped = null, amuletEquipped = null;
	    if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
	      attack = (int) (ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED/2));
	      defense = (int) (ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED/2));
	      weaponEquipped = ControllerConstants.ARCHER_WEAPON_ID_LEVEL[equipmentLevel-1];
	      armorEquipped = ControllerConstants.ARCHER_ARMOR_ID_LEVEL[equipmentLevel-1];
	      amuletEquipped = ControllerConstants.ALL_CHARACTERS_EQUIP_LEVEL[equipmentLevel-1];
	    }
	    if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
	      attack = (int) (ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED*(1-characterMultiplier)));
	      defense = (int) (ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED*characterMultiplier));
	      weaponEquipped = ControllerConstants.WARRIOR_WEAPON_ID_LEVEL[equipmentLevel-1];
	      armorEquipped = ControllerConstants.WARRIOR_ARMOR_ID_LEVEL[equipmentLevel-1];
	      amuletEquipped = ControllerConstants.ALL_CHARACTERS_EQUIP_LEVEL[equipmentLevel-1];
	    }
	    if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
	      attack = (int) (ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED*characterMultiplier));
	      defense = (int) (ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED*(1-characterMultiplier)));
	      weaponEquipped = ControllerConstants.MAGE_WEAPON_ID_LEVEL[equipmentLevel-1];
	      armorEquipped = ControllerConstants.MAGE_ARMOR_ID_LEVEL[equipmentLevel-1];
	      amuletEquipped = ControllerConstants.ALL_CHARACTERS_EQUIP_LEVEL[equipmentLevel-1];
	    }
	    
	    //Add randomization
	    if ((int)Math.floor(Math.random() * 5) + 1 < 2) {
	    	attack += (int)Math.floor(Math.random() * 3) + 1;
	    } else {
	    	attack -= (int)Math.floor(Math.random() * 3) + 1;
	    }
	    
	    if ((int)Math.floor(Math.random() * 5) + 1 < 2) {
	    	defense += (int)Math.floor(Math.random() * 3) + 1;
	    } else {
	    	defense -= (int)Math.floor(Math.random() * 3) + 1;
	    }
	    
	    if ((int)Math.floor(Math.random() * 5) + 1 < 2) {
	    	health += (int)Math.floor(Math.random() * 5) + 1;
	    } else {
	    	health -= (int)Math.floor(Math.random() * 5) + 1;
	    }
	    if (health<30) {
	    	health = 30;
	    }
	    
	    initialized[0] = attack;
	    initialized[1] = defense;
	    initialized[2] = health;
	    initialized[3] = weaponEquipped;
	    initialized[4] = armorEquipped;
	    initialized[5] = amuletEquipped;
	    return initialized;
	    
  }

}
