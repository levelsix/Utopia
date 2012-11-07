package com.lvl6.scriptsjava.generatefakeusers;

import java.io.IOException;
import java.util.Random;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.lvl6.info.Location;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GenerateFakeUsersWithoutInput {

  private static String nameRulesFile = "src/main/java/com/lvl6/scriptsjava/generatefakeusers/namerulesElven.txt";
  private static int numEnemiesToCreatePerLevel = 300;
  private static int minLevel = 41;
  private static int maxLevel = 50;

  private static int syllablesInName1 = 2;
  private static int syllablesInName2 = 3;

  protected InsertUtil insertUtils;

  public static void main(String[] args) {
    ApplicationContext context = new FileSystemXmlApplicationContext("target/utopia-server-1.0-SNAPSHOT/WEB-INF/spring-application-context.xml");
    NameGenerator nameGenerator = null;
    Random random = new Random();
    try {
      nameGenerator = new NameGenerator(nameRulesFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (nameGenerator != null) {
      System.out.println("beginning!");
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
    
    InsertUtil insertUtils = (InsertUtil) AppContext.getApplicationContext().getBean("insertUtils");

    if (insertUtils.insertUser(newReferCode + newReferCode, name, type, location, null, newReferCode, level, 
        attack, defense, 0, 0, 0, 0, 0, null, null, null, true, ControllerConstants.PURCHASE_GROUP_CHAT__NUM_CHATS_GIVEN_FOR_PACKAGE) < 0) {
      System.out.println("error in creating user");
    }

    System.out.println("created "+name);
  }
  
  public static int[] initializeFakePlayerStats(UserType type, int level) {
	  int attack = 0;
	    int defense = 0;
	    int equipmentLevel = level;
	    double characterMultiplier = ControllerConstants.CHARACTERS_ATTACK_DEFENSE_VARIABILITY;
	    int[] initialized = new int[2];
	    
	    if (equipmentLevel > ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER) {
	    	equipmentLevel = ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER;
	    }
	    
	    if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
	      attack = (int) (ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED/2));
	      defense = (int) (ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED/2));
	    }
	    if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
	      attack = (int) (ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED*(1-characterMultiplier)));
	      defense = (int) (ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED*characterMultiplier));
	    }
	    if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
	      attack = (int) (ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_ATTACK_GAINED*characterMultiplier));
	      defense = (int) (ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE 
	    		  + Math.ceil((level-1)*ControllerConstants.LEVEL_UP_DEFENSE_GAINED*(1-characterMultiplier)));
	    }
	    
	    //Add randomization
	    if (Math.random() < 0.5) {
	    	attack += (int)Math.floor(Math.random() * 3);
	    } else {
	    	attack -= (int)Math.floor(Math.random() * 3);
	    }
	    
	    if (Math.random() < 0.5) {
	    	defense += (int)Math.floor(Math.random() * 3);
	    } else {
	    	defense -= (int)Math.floor(Math.random() * 3);
	    }
	    
	    initialized[0] = attack;
	    initialized[1] = defense;
	    
	    return initialized;
	    
  }

}
