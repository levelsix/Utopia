package com.lvl6.properties;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.ValidLocationBox;

public class ControllerConstants {

  public static final int NOT_SET = -1;
  
  //LOCATION RESTRICTIONS
  public static final int LONGITUDE_MIN = -180;
  public static final int LONGITUDE_MAX = 180;
  public static final int LATITUDE_MIN = -90;
  public static final int LATITUDE_MAX = 90;

  //GENERATING LISTS OF ENEMIES
  public static final int NUM_MINUTES_SINCE_LAST_BATTLE_BEFORE_APPEARANCE_IN_ATTACK_LISTS = 10;
  
  //CRIT STRUCTS
  public static final CoordinatePair CARPENTER_COORDS = new CoordinatePair(10, 6);
  public static final CoordinatePair AVIARY_COORDS = new CoordinatePair(10, 10);
  
  public static final int ARMORY_XLENGTH = 2;
  public static final int ARMORY_YLENGTH = 2;
  public static final int ARMORY_IMG_VERTICAL_PIXEL_OFFSET = 0;
  public static final int VAULT_XLENGTH = 2;
  public static final int VAULT_YLENGTH = 2;
  public static final int VAULT_IMG_VERTICAL_PIXEL_OFFSET = 0;
  public static final int MARKETPLACE_XLENGTH = 2;
  public static final int MARKETPLACE_YLENGTH = 2;
  public static final int MARKETPLACE_IMG_VERTICAL_PIXEL_OFFSET = 0;
  public static final int CARPENTER_XLENGTH = 2;
  public static final int CARPENTER_YLENGTH = 2;
  public static final int CARPENTER_IMG_VERTICAL_PIXEL_OFFSET = 0;
  public static final int AVIARY_XLENGTH = 2;
  public static final int AVIARY_YLENGTH = 2;
  public static final int AVIARY_IMG_VERTICAL_PIXEL_OFFSET = 0;
  
  public static final int MIN_LEVEL_FOR_ARMORY = 3;
  public static final int MIN_LEVEL_FOR_VAULT = 4;
  public static final int MIN_LEVEL_FOR_MARKETPLACE = 5;

  //--------------------------------------------------------------------------------------------------------------------------
  
  //FORMULA CONSTANTS (ALSO) SENT TO CLIENT
  public static final double MINUTES_TO_UPGRADE_FOR_NORM_STRUCT_MULTIPLIER = .5;
  public static final double INCOME_FROM_NORM_STRUCT_MULTIPLIER = 1;
  public static final double UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_EXPONENT_BASE = 1.7;
  public static final double UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_EXPONENT_BASE = 1.1;
  public static final double FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS__DIAMOND_COST_FOR_INSTANT_UPGRADE_MULTIPLIER = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_ATTACK_STAT = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_ATTACK_EQUIP_SUM = .5;
  public static final double BATTLE_WEIGHT_GIVEN_TO_DEFENSE_STAT = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_DEFENSE_EQUIP_SUM = .5;
  
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_FIRST_EXPANSION = 3;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_SECOND_EXPANSION = 12;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_THIRD_EXPANSION = 24;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_FOURTH_EXPANSION = 30;
  public static final int EXPANSION_WAIT_COMPLETE__HOUR_INCREMENT_BETWEEN_LATER_LEVELS = 4;
  
  //--------------------------------------------------------------------------------------------------------------------------

  //TUTORIAL CONSTANTS
  public static final int TUTORIAL__ARCHER_INIT_ATTACK = 17;
  public static final int TUTORIAL__ARCHER_INIT_DEFENSE = 12;
  public static final int TUTORIAL__MAGE_INIT_ATTACK = 12;
  public static final int TUTORIAL__MAGE_INIT_DEFENSE = 20;
  public static final int TUTORIAL__WARRIOR_INIT_ATTACK = 15;
  public static final int TUTORIAL__WARRIOR_INIT_DEFENSE = 15;
  public static final int TUTORIAL__INIT_ENERGY = 20;
  public static final int TUTORIAL__INIT_STAMINA = 3;
  public static final int TUTORIAL__INIT_HEALTH = 30;
  public static final int TUTORIAL__INIT_DIAMONDS = 10;
  public static final int TUTORIAL__INIT_COINS = 50;
  public static final int TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT = 2;
  public static final int TUTORIAL__FIRST_TASK_ID = 1;          //give user full task proto for both sides
  public static final int TUTORIAL__ARCHER_INIT_WEAPON_ID = 120;
  public static final int TUTORIAL__ARCHER_INIT_ARMOR_ID = 159;
  public static final int TUTORIAL__MAGE_INIT_WEAPON_ID = 185;
  public static final int TUTORIAL__MAGE_INIT_ARMOR_ID = 224;
  public static final int TUTORIAL__WARRIOR_INIT_WEAPON_ID = 1;
  public static final int TUTORIAL__WARRIOR_INIT_ARMOR_ID = 41;
  public static final String TUTORIAL__FAKE_QUEST_GOOD_NAME = "Cold Welcome";
  public static final String TUTORIAL__FAKE_QUEST_BAD_NAME = "Cold Welcome";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION = "Welcome to Kirin Village, stranger. We are in dire times and we need your help. A few moments ago, a strange white light consumed the sky and legion soldiers appeared out of thin air. One seems to have strayed from the pack. Start by killing him!";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION = "Welcome to Kirin Village, soldier. A few moments ago, a strange white light consumed the sky and blinded our eyes during our siege against the Alliance. We found ourselves here with a few Lumorian soldiers after we regained our vision. One seems to have strayed from the pack. Start by killing him!";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE = "Simply amazing! Your battle prowess makes our village seem safer already. ";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE = "Excellent work soldier. Good to know I have a competent ally watching my back.";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_IN_PROGRESS = "Back already? Don't tell me you couldn't even complete these measy tasks!";
  public static final String TUTORIAL__FAKE_QUEST_BAD_IN_PROGRESS = "Back already? Don't tell me you couldn't even complete these measy tasks!";
  public static final int TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY = 1;
  public static final int TUTORIAL__FAKE_QUEST_COINS_GAINED = 30;
  public static final int TUTORIAL__FAKE_QUEST_EXP_GAINED = 4;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN = 20;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN = 4;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID = 250;
  public static final int TUTORIAL__FIRST_STRUCT_TO_BUILD = 1;
  public static final int TUTORIAL__FIRST_NEUTRAL_CITY_ID = 1;
  
  //ARMORY
  public static final double ARMORY__SELL_RATIO = 0.15;
  
  //BATTLE
  public static final int BATTLE__MAX_ITEMS_USED = 4;   //unused right now
  public static final int BATTLE__MAX_LEVEL_DIFFERENCE = 10;
  public static final double BATTLE__A = .2;		//must be <= 1
  public static final double BATTLE__B = 80;
  public static final double BATTLE__EXP_GAIN_LOWER_BOUND = .8;
  public static final double BATTLE__EXP_GAIN_UPPER_BOUND = 1.2;
  public static final double BATTLE__EXP_GAIN_MULTIPLIER = .6;
  
  public static final float BATTLE__LOCATION_BAR_MAX = 75.f;
  public static final double BATTLE__MAX_ATTACK_MULTIPLIER = 1.5;
  public static final double BATTLE__MIN_PERCENT_OF_ENEMY_HEALTH = .25;
  public static final double BATTLE__MAX_PERCENT_OF_ENEMY_HEALTH = .6;
  public static final double BATTLE__BATTLE_DIFFERENCE_MULTIPLIER = 2;
  public static final double BATTLE__BATTLE_DIFFERENCE_TUNER = 0;
  
  //GENERATE ATTACK LIST
  public static final int GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX = 25;
  
  //POST TO MARKETPLACE
  public static final int POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER = 50;  
  
  //PURCHASE FROM MARKETPLACE
  public static final double PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .10;
  
  //TASK ACTION
  public static final int TASK_ACTION__MAX_CITY_RANK = 5;
  
  //PURCHASE NORM STRUCTURE
  public static final int PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE = 2;

  //UPGRADE NORM STRUCTURE
  public static final int UPGRADE_NORM_STRUCTURE__MAX_STRUCT_LEVEL = 5;
  
  //SELL NORM STRUCTURE
  public static final double SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER = .2;
  
  //RETRIEVE CURRENT MARKETPLACE POSTS
  public static final int RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP = 100;

  //RETRACT MARKETPLACE POST
  public static final double RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .1;
  
  //USE SKILL POINT
  public static final int USE_SKILL_POINT__MAX_STAT_GAIN = 5;   //right now its health
  public static final int USE_SKILL_POINT__ATTACK_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__HEALTH_BASE_GAIN = 5;
  public static final int USE_SKILL_POINT__STAMINA_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ATTACK_BASE_COST = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_COST = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_COST = 1;
  public static final int USE_SKILL_POINT__HEALTH_BASE_COST = 1;
  public static final int USE_SKILL_POINT__STAMINA_BASE_COST = 2;
  
  //VAULT
  public static final double VAULT__DEPOSIT_PERCENT_CUT = 0.1;
  
  //REFILL STAT WITH DIAMONDS
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL = 10;
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL = 10;

  //LOAD PLAYER CITY
  public static final int LOAD_PLAYER_CITY__APPROX_NUM_USERS_IN_CITY = 4;
  
  //REFILL STAT WAIT COMPLETE
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA = 4;
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY = 3;

  //PURCHASE MARKETPLACE LICENSE
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE = 30;
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE = 3;
  public static final int PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST = 40;
  public static final int PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST = 5;

  //USER CREATE 
  public static final int USER_CREATE__START_LEVEL = 2;
  public static final int USER_CREATE__MIN_NAME_LENGTH = 3;
  public static final int USER_CREATE__MAX_NAME_LENGTH = 15;
  public static final int USER_CREATE__MIN_COIN_REWARD_FOR_REFERRER = 100;
  public static final int USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED = 50;
  public static final double USER_CREATE__PERCENTAGE_OF_COIN_WEALTH_GIVEN_TO_REFERRER = .2;
  
  //LEVEL UP
  public static final int LEVEL_UP__SKILL_POINTS_GAINED = 3;
  public static final int LEVEL_UP__MAX_LEVEL_FOR_USER = 35;

  //POST_ON_PLAYER_WALL
  public static final int POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH = 1000;

  //RETRIEVE PLAYER WALL POSTS
  public static final int RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP = 15;
  
  public static final ValidLocationBox[] USER_CREATE__VALIDATION_BOXES = { 
    new ValidLocationBox(-117.69765, 33.57793, 26.77272, 12.027776, "US"),
    new ValidLocationBox(-118.76606, 50.595863, 27.16478, 9.0692883, "CANADA"),
    new ValidLocationBox(-108.42838, 25.134665, 10.040737, 5.1833048, "MEXICO"),
    new ValidLocationBox(-75.779137, -9.728591, 21.797827, 12.667298, "SOUTH AMERICA TOP"),
    new ValidLocationBox(-4.8001895, 7.449348, 43.104004, 23.590857, "AFRICA TOP"),
    new ValidLocationBox(17.695606, -18.203896, 19.675283, 11.1834, "AFRICA BOTTOM"),
    new ValidLocationBox(50.203793, 41.678776, 67.458984, 23.556379, "RUSSIA"),
    new ValidLocationBox(7.239326, 45.496315, 22.494024, 8.4832458, "EUROPE RIGHT"),
    new ValidLocationBox(-50.585747, 70.842873, 19.442516, 3.4063075, "GREENLAND"),
    new ValidLocationBox(122.57473, -29.775003, 22.857393, 12.192301, "GREENLAND")};
  
}
