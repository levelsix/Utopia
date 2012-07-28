package com.lvl6.properties;

import com.lvl6.info.AnimatedSpriteOffset;
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
  
  public static final int DEFAULT_USER_EQUIP_LEVEL = 1;
  
  //--------------------------------------------------------------------------------------------------------------------------
  
  //FORMULA CONSTANTS (ALSO) SENT TO CLIENT
  public static final double MINUTES_TO_UPGRADE_FOR_NORM_STRUCT_MULTIPLIER = .5;
  public static final double INCOME_FROM_NORM_STRUCT_MULTIPLIER = 1;
  public static final double UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_EXPONENT_BASE = 1.7;
  public static final double UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_EXPONENT_BASE = 1.1;
  public static final double FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS__DIAMOND_COST_FOR_INSTANT_UPGRADE_MULTIPLIER = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_ATTACK_STAT = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_ATTACK_EQUIP_SUM = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_DEFENSE_STAT = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_DEFENSE_EQUIP_SUM = 1;
  public static final double BATTLE_WEIGHT_GIVEN_TO_LEVEL = 1;
  public static final float BATTLE_LOCATION_BAR_MAX = 75.f;
  public static final float BATTLE_PERFECT_PERCENT_THRESHOLD = 3.0f;
  public static final float BATTLE_GREAT_PERCENT_THRESHOLD = 17.0f;
  public static final float BATTLE_GOOD_PERCENT_THRESHOLD = 38.0f;
  public static final float BATTLE_PERFECT_MULTIPLIER = 2.0f;
  public static final float BATTLE_GREAT_MULTIPLIER = 1.5f;
  public static final float BATTLE_GOOD_MULTIPLIER = 1.0f;
  public static final float BATTLE_IMBALANCE_PERCENT = .67f;
  public static final float BATTLE_PERFECT_LIKELIHOOD = .25f;
  public static final float BATTLE_GREAT_LIKELIHOOD = .5f;
  public static final float BATTLE_GOOD_LIKELIHOOD = .15f;
  public static final float BATTLE_MISS_LIKELIHOOD = .1f;

  public static final double BATTLE__HIT_ATTACKER_PERCENT_OF_HEALTH = 0.2;
  public static final double BATTLE__HIT_DEFENDER_PERCENT_OF_HEALTH = 0.25;
  public static final double BATTLE__PERCENT_OF_WEAPON = 1.0/9.0;
  public static final double BATTLE__PERCENT_OF_ARMOR = 1.0/9.0;  
  public static final double BATTLE__PERCENT_OF_AMULET = 1.0/9.0;
  public static final double BATTLE__PERCENT_OF_PLAYER_STATS = 3.0/9.0;
  public static final double BATTLE__ATTACK_EXPO_MULTIPLIER = 0.8;
  public static final double BATTLE__PERCENT_OF_EQUIPMENT = 3.0/9.0;      
  public static final double BATTLE__INDIVIDUAL_EQUIP_ATTACK_CAP = 5.0; 
  
  
  public static final double HEALTH__FORMULA_EXPONENT_BASE = 1.18;
  
  public static final int AVERAGE_SIZE_OF_LEVEL_BRACKET = 5;

  public static final int FORGE_MIN_DIAMOND_COST_FOR_GUARANTEE = 5;
  public static final double FORGE_DIAMOND_COST_FOR_GUARANTEE_EXPONENTIAL_MULTIPLIER = 2;
  public static final int FORGE_MAX_EQUIP_LEVEL = 10;
  public static final int FORGE_BASE_MINUTES_TO_ONE_GOLD = 6;
  public static final double FORGE_TIME_BASE_FOR_EXPONENTIAL_MULTIPLIER = 1.8;
  public static final double FORGE_LEVEL_EQUIP_BOOST_EXPONENT_BASE = 1.2;
  
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_FIRST_EXPANSION = 3;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_SECOND_EXPANSION = 12;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_THIRD_EXPANSION = 24;
  public static final int EXPANSION_WAIT_COMPLETE__HOURS_FOR_FOURTH_EXPANSION = 30;
  public static final int EXPANSION_WAIT_COMPLETE__HOUR_INCREMENT_BETWEEN_LATER_LEVELS = 4;
  
  //--------------------------------------------------------------------------------------------------------------------------

  //TUTORIAL CONSTANTS
  public static final double CHARACTERS_ATTACK_DEFENSE_VARIABILITY = 0.67;
  public static final int TUTORIAL__ARCHER_INIT_ATTACK = 12; 
  public static final int TUTORIAL__ARCHER_INIT_DEFENSE = 12;
  public static final int TUTORIAL__MAGE_INIT_ATTACK = 14; 
  public static final int TUTORIAL__MAGE_INIT_DEFENSE = 10;
  public static final int TUTORIAL__WARRIOR_INIT_ATTACK = 10;
  public static final int TUTORIAL__WARRIOR_INIT_DEFENSE = 14;
  public static final int TUTORIAL__INIT_ENERGY = 20;
  public static final int TUTORIAL__INIT_STAMINA = 3;
  public static final int TUTORIAL__INIT_HEALTH = 30;
  public static final int TUTORIAL__INIT_DIAMONDS = 20;
  public static final int TUTORIAL__INIT_COINS = 50;
  public static final int TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT = 0; //Because it does not warn the user
  public static final int TUTORIAL__FIRST_TASK_ID = 1;          //give user full task proto for both sides
  public static final int TUTORIAL__ARCHER_INIT_WEAPON_ID = 120;
  public static final int TUTORIAL__ARCHER_INIT_ARMOR_ID = 159;
  public static final int TUTORIAL__MAGE_INIT_WEAPON_ID = 185;
  public static final int TUTORIAL__MAGE_INIT_ARMOR_ID = 224;
  public static final int TUTORIAL__WARRIOR_INIT_WEAPON_ID = 1;
  public static final int TUTORIAL__WARRIOR_INIT_ARMOR_ID = 41;
  public static final String TUTORIAL__FAKE_QUEST_GOOD_NAME = "Preserve the Peace";
  public static final String TUTORIAL__FAKE_QUEST_BAD_NAME = "Witness Protection";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_ACCEPT_DIALOGUE = "10~good~";
  public static final String TUTORIAL__FAKE_QUEST_BAD_ACCEPT_DIALOGUE = "10~bad~";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION = "Welcome to Kirin Village, stranger. We are in dire times and we need your help. A few moments ago, a strange white light consumed the sky and legion soldiers appeared out of thin air. One seems to have strayed from the pack. Start by killing him!";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION = "Welcome to Kirin Village, soldier. A few moments ago, a strange white light consumed the sky and blinded our eyes during our siege against the Alliance. We found ourselves here with a few Lumorian soldiers after we regained our vision. One seems to have strayed from the pack. Start by killing him!";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE = "Simply amazing! Your battle prowess makes our village seem safer already. ";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE = "Excellent work soldier. Good to know I have a competent ally watching my back.";
  public static final int TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY = 5;
  public static final int TUTORIAL__FAKE_QUEST_COINS_GAINED = 8;
  public static final int TUTORIAL__FAKE_QUEST_EXP_GAINED = 4;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN = 5;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN = 1;
  public static final int TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID = 250;
  public static final int TUTORIAL__FIRST_STRUCT_TO_BUILD = 1;
  public static final int TUTORIAL__FIRST_NEUTRAL_CITY_ID = 1;
  
  //STARTUP
  public static final int STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND = 20;
  public static final int STARTUP__HOURS_OF_BATTLE_NOTIFICATIONS_TO_SEND = 24*2;
  public static final int STARTUP__APPROX_NUM_ALLIES_TO_SEND = 20;
  public static final int STARTUP__DAILY_BONUS_TIME_REQ_BETWEEN_CONSEC_DAYS = 1; //in days
  public static final int STARTUP__DAILY_BONUS_SMALL_BONUS_COIN_QUANTITY = 5;
  public static final int STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_SMALL_BONUS = 1;
  public static final int STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_BIG_BONUS = 5;
  public static final int STARTUP__DAILY_BONUS_MAX_CONSEC_DAYS_BIG_BONUS = 5;
  public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_COMMON_EQUIP = 0;    //total should add up to 1
  public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_UNCOMMON_EQUIP = 0.8;
  public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_RARE_EQUIP = 0.15;
  public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_EPIC_EQUIP = 0.05;
  public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_LEGENDARY_EQUIP = 0;
  public static final int STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE = 5;
  public static final int STARTUP__DAILY_BONUS_MYSTERY_BOX_EQUIP_FORGE_LEVEL_MAX = 2;
  
  //ARMORY
  public static final double ARMORY__SELL_RATIO = 0.15;
  
  //BATTLE
  public static final int BATTLE__MAX_ITEMS_USED = 4;   //unused right now
  public static final int BATTLE__MAX_LEVEL_DIFFERENCE = 3;
  public static final double BATTLE__A = .2;		//must be <= 1
  public static final double BATTLE__B = 80;
  public static final int BATTLE__MIN_COINS_FROM_WIN = 5;
  public static final double BATTLE__EXP_NUM_KILLS_CONSTANT = 1.25;
  public static final int BATTLE__EXP_MIN_NUM_KILLS = 2;
  public static final double BATTLE__EXP_WEIGHT_GIVEN_TO_BATTLES = 0.33;

  //GENERATE ATTACK LIST
  public static final int GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX = 25;
  
  //POST TO MARKETPLACE
  public static final int POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER = 50;  
  
  //PURCHASE FROM MARKETPLACE
  public static final double PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .30;
  
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
  public static final int RETRIEVE_CURRENT_MARKETPLACE_POSTS__MAX_NUM_POPULATE_RETRIES = 5;
  public static final int RETRIEVE_CURRENT_MARKETPLACE_POSTS__MIN_NUM_OF_POSTS_FOR_NO_POPULATE = 15;
  public static final int RETRIEVE_CURRENT_MARKETPLACE_POSTS__EQUIP_ID_TO_POPULATE = 250;   //ANYONE SHOULD BE ABLE TO EQUIP, and should be cheap AND ACCESSIBLE IN ARMORY and needs to have a coin cost
  public static final int[] RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_POSTER_IDS = {2};
  public static final double RETRIEVE_CURRENT_MARKETPLACE_POSTS__FAKE_EQUIP_PERCENT_OF_ARMORY_PRICE_LISTING = 0.9; 
  
  //RETRACT MARKETPLACE POST
  public static final double RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .1;
  
  //USE SKILL POINT
  public static final int USE_SKILL_POINT__MAX_STAT_GAIN = 1;
  public static final int USE_SKILL_POINT__ATTACK_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__STAMINA_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ATTACK_BASE_COST = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_COST = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_COST = 1;
  public static final int USE_SKILL_POINT__STAMINA_BASE_COST = 2;
  
  //VAULT
  public static final double VAULT__DEPOSIT_PERCENT_CUT = 0.1;
  
  //REFILL STAT WITH DIAMONDS
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL = 10;
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL = 10;

  //LOAD PLAYER CITY
  public static final int LOAD_PLAYER_CITY__APPROX_NUM_USERS_IN_CITY = 4;
  
  //REFILL STAT WAIT COMPLETE
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA = 10;
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY = 3;

  //PURCHASE MARKETPLACE LICENSE
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE = 30;
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE = 3;
  public static final int PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST = 40;
  public static final int PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST = 5;

  //USER CREATE 
  public static final int USER_CREATE__START_LEVEL = 2;
  public static final int USER_CREATE__MIN_NAME_LENGTH = 1;
  public static final int USER_CREATE__MAX_NAME_LENGTH = 15;
  public static final int USER_CREATE__MIN_COIN_REWARD_FOR_REFERRER = 100;
  public static final int USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED = 50;
  public static final double USER_CREATE__PERCENTAGE_OF_COIN_WEALTH_GIVEN_TO_REFERRER = .2;
  public static final int USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL = 1;
  public static final String USER_CREATE__FIRST_WALL_POST_TEXT = "Hey! My name's Alex, one of the creators of this game. We hope you enjoy it! :)";
  
  //LEVEL UP
  public static final int LEVEL_UP__SKILL_POINTS_GAINED = 3;
  public static final int LEVEL_UP__MAX_LEVEL_FOR_USER = 30; //add level up equipment for fake players if increasing
//  public static final double LEVEL_UP_HEALTH_GAINED = 5.0;
  public static final double LEVEL_UP_ATTACK_GAINED = 2.0;
  public static final double LEVEL_UP_DEFENSE_GAINED = 2.0;
  
  //LEVEL UP EQUIPMENT FOR FAKE PLAYERS (levels 1-30 must add more if going above level 30)
  public static final int[] WARRIOR_WEAPON_ID_LEVEL = {1,1,1,3,3, 5,5,6,6,7, 
	  11,11,12,12,13, 17,18,19,19,20, 24,25,26,26,27, 31,32,32,33,34};
  public static final int[] WARRIOR_ARMOR_ID_LEVEL = {41,41,41,43,43, 44,44,45,45,45, 
	  47,47,47,48,48, 51,51,51,67,67, 71,71,72,72,72, 76,76,76,77,77};
  public static final int[] ARCHER_WEAPON_ID_LEVEL = {120,120,120,122,122, 124,124,125,126,126,
	  130,130,131,131,132, 136,137,138,138,139, 143,144,144,145,146, 150,151,151,152,153};
  public static final int[] ARCHER_ARMOR_ID_LEVEL = {159,159,159,161,161, 162,162,162,163,163, 
	  165,165,165,166,166, 169,169,169,170,170, 174,174,175,175,175, 179,179,179,180,180};
  public static final int[] MAGE_WEAPON_ID_LEVEL = {185,185,185,187,187, 189,189,190,191,191, 
	  195,195,196,196,197, 201,202,203,203,204, 208,209,209,210,211, 215,216,216,217,218};
  public static final int[] MAGE_ARMOR_ID_LEVEL = {224,224,224,226,226, 227,227,227,228,228, 
	  230,230,230,231,231, 234,234,234,235,235, 239,239,240,240,240, 244,244,244,245,245};
  public static final int[] ALL_CHARACTERS_EQUIP_LEVEL = {250,250,250,251,252, 254,254,255,255,256,
	  259,260,260,261,261, 261,264,265,266,266, 270,270,271,271,272, 276,276,276,277,277};
  
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

  public static final AnimatedSpriteOffset[] STARTUP__ANIMATED_SPRITE_OFFSETS = {
    new AnimatedSpriteOffset("TutorialGuide", new CoordinatePair(0, -9)),
    new AnimatedSpriteOffset("TutorialGuideBad", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("AllianceArcher", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("AllianceWarrior", new CoordinatePair(0, -15)),
    new AnimatedSpriteOffset("AllianceMage", new CoordinatePair(0, -26)),
    new AnimatedSpriteOffset("LegionArcher", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("LegionWarrior", new CoordinatePair(0, -15)),
    new AnimatedSpriteOffset("LegionMage", new CoordinatePair(0, -18)),
    new AnimatedSpriteOffset("Bandit", new CoordinatePair(0, -15)),
    new AnimatedSpriteOffset("FarmerMitch", new CoordinatePair(0, -8)),
    new AnimatedSpriteOffset("Carpenter", new CoordinatePair(0, -8)),
  };
  
  public static final String[] STARTUP__NOTICES_TO_PLAYERS = {};
  
  
  public static final int[] STARTUP__LEVELS_THAT_TRIGGER_KIIP_REWARDS = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 
    19, 20, 21, 23, 24, 25, 26};
  public static final int[] STARTUP__QUESTS_THAT_TRIGGER_KIIP_REWARDS_ON_REDEEM = {};

}
