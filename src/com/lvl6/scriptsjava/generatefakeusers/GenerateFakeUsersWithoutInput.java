package com.lvl6.scriptsjava.generatefakeusers;

import java.io.IOException;
import java.util.Random;

import com.lvl6.info.Location;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GenerateFakeUsersWithoutInput {

  private static String nameRulesFile = "src/com/lvl6/scriptsjava/generatefakeusers/namerulesElven.txt";
  private static int numEnemiesToCreatePerLevel = 75;
  private static int minLevel = 1;
  private static int maxLevel = 25;
  private static int syllablesInName = 2;

  public static void main(String[] args) {
    NameGenerator nameGenerator = null;
    Random random = new Random();
    try {
      nameGenerator = new NameGenerator(nameRulesFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (nameGenerator != null) {
      DBConnection.init();
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
  //none of the players were referred
  private static void createUser(Random random, NameGenerator nameGenerator, int level) {
    String name = nameGenerator.compose(syllablesInName);
    UserType type = UserType.valueOf(random.nextInt(UserType.values().length));

    Location location = MiscMethods.getRandomValidLocation();
    
    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    } else {
      //TODO: generate more codes?
    }
    
    
    int skillPoints = (level-1)*ControllerConstants.LEVEL_UP__SKILL_POINTS_GAINED;
    int attack = 1;
    int defense = 1;
    if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
      attack = ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK;
      defense = ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE;
    }
    if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
      attack = ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK;
      defense = ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE;      
    }
    if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
      attack = ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK;
      defense = ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE;
    } else {
      System.out.println("problem with user type");
    }
    int stamina = ControllerConstants.TUTORIAL__INIT_STAMINA;
    int energy = ControllerConstants.TUTORIAL__INIT_ENERGY;
    int health = ControllerConstants.TUTORIAL__INIT_HEALTH;

    for (int i = 0; i < skillPoints; i++) {
      int rint = (int)Math.floor(Math.random() * 5) + 1;
      switch (rint) {
      case 1:
        attack++;
        break;
      case 2:
        defense++;
        break;
      case 3:
        stamina++;
        break;
      case 4:
        health++;
        break;
      case 5:
        energy++;
        break;
      }
    }
    if (InsertUtils.insertUser(newReferCode+newReferCode, name, type, location, false, null, newReferCode, level, attack, defense, energy, health, stamina) < 0) {
      System.out.println("error in creating user");
    }
  }

}
