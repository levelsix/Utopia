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

public class GenerateFakeUsersWithoutInput {

  private static String nameRulesFile = "src/com/lvl6/scriptsjava/generatefakeusers/namerulesElven.txt";
  private static int numEnemiesToCreatePerLevel = 100;
  private static int minLevel = 17;
  private static int maxLevel = 50;
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

  
  //UDIDs and deviceTokens are null
  //none of the players were referred
  private static void createUser(Random random, NameGenerator nameGenerator, int level) {
    String name = nameGenerator.compose(syllablesInName);
    UserType type = UserType.valueOf(random.nextInt(UserType.values().length));

    double latitude = ControllerConstants.LATITUDE_MIN + random.nextDouble()*(ControllerConstants.LATITUDE_MAX - ControllerConstants.LATITUDE_MIN);
    double longitude = ControllerConstants.LONGITUDE_MIN + random.nextDouble()*(ControllerConstants.LONGITUDE_MAX - ControllerConstants.LONGITUDE_MIN);

    Location location = new Location(latitude, longitude);
    
    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    }
    
    if (InsertUtils.insertUser(null, name, type, location, false, null, newReferCode, level) < 0) {
      System.out.println("error in creating user");
    }
  }

}
