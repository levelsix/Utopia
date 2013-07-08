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
  public static final int DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT = 0;
  public static final String UER__BOSS_ACTION = "boss action";
  public static final String UER__BATTLE = "battle";
  public static final String UER__COLLECT_EQUIP_ENHANCEMENT = "collect equip enhancement";
  public static final String UER__SUCCESSFUL_FORGE = "successful forge";
  public static final String UER__UNSUCCESSFUL_FORGE = "unsuccessful forge";
  public static final String UER__PICK_LOCK_BOX = "pick lock box";
  public static final String UER__THREE_CARD_MONTE = "three card monte";
  public static final String UER__PURCHASE_BOOSTER_PACK = "purhcase booster pack";
  public static final String UER__PURCHASE_FROM_MARKETPLACE = "purchase from marketplace";
  public static final String UER__QUEST_REDEEM = "quest redeem";
  public static final String UER__REDEEM_USER_CITY_GEMS = "redeem user city gems";
  public static final String UER__REDEEM_USER_LOCK_BOX_ITEMS = "redeem user lock box items";
  public static final String UER__DAILY_BONUS_REWARD = "daily bonus reward";
  public static final String UER__RETRACT_MARKETPLACE_POST = "retract_marketplace_post";
  public static final String UER__TASK_ACTION = "task action";
  public static final String UER__USER_CREATED = "user created";
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
  public static final float BATTLE_LOCATION_BAR_MAX = 83.33f;
  public static final float BATTLE_PERFECT_PERCENT_THRESHOLD = 3.0f;
  public static final float BATTLE_GREAT_PERCENT_THRESHOLD = 17.0f;
  public static final float BATTLE_GOOD_PERCENT_THRESHOLD = 38.0f;
  public static final float BATTLE_PERFECT_MULTIPLIER = 2.0f;
  public static final float BATTLE_GREAT_MULTIPLIER = 1.5f;
  public static final float BATTLE_GOOD_MULTIPLIER = 1.0f;
  public static final float BATTLE_IMBALANCE_PERCENT = .67f;
  public static final float BATTLE_PERFECT_LIKELIHOOD = .25f;
  public static final float BATTLE_GREAT_LIKELIHOOD = .55f;
  public static final float BATTLE_GOOD_LIKELIHOOD = .15f;
  public static final float BATTLE_MISS_LIKELIHOOD = .05f;

  public static final double BATTLE__HIT_ATTACKER_PERCENT_OF_HEALTH = 0.2;
  public static final double BATTLE__HIT_DEFENDER_PERCENT_OF_HEALTH = 0.25;
  public static final double BATTLE__PERCENT_OF_WEAPON = 1.0/9.0;
  public static final double BATTLE__PERCENT_OF_ARMOR = 1.0/9.0;  
  public static final double BATTLE__PERCENT_OF_AMULET = 1.0/9.0;
  public static final double BATTLE__PERCENT_OF_PLAYER_STATS = 3.0/9.0;
  public static final double BATTLE__ATTACK_EXPO_MULTIPLIER = 0.8;
  public static final double BATTLE__PERCENT_OF_EQUIPMENT = 3.0/9.0;      
  public static final double BATTLE__INDIVIDUAL_EQUIP_ATTACK_CAP = 5.0; 
  public static final double BATTLE__FAKE_PLAYER_COIN_GAIN_MULTIPLIER = 3;
  public static final double BATTLE__CHANCE_OF_ZERO_GAIN_FOR_SILVER = .2;
  
  //old boss constants
  public static final double BOSS_EVENT__SUPER_ATTACK = 3.0;
  public static final int BOSS_EVENT__NUMBER_OF_ATTACKS_UNTIL_SUPER_ATTACK = 5;
  //revamped boss constants
  //ublic static final int SOLO_BOSS__ATTACK_COST = 1;
  //public static final int SOLO_BOSS__SUPER_ATTACK_COST = 3;
  //public static final double SOLO_BOSS__SUPER_ATTACK_DAMGE_MULTIPLIER = 1.5; 
  
  //new revamped boss constants
  public static final double SOLO_BOSS__CRITICAL_HIT_CHANCE = 0.15;
  public static final double SOLO_BOSS__CRITICAL_HIT_DAMAGE_MULTIPLIER = 1.6;
  public static final int SOLO_BOSS__MAX_HEALTH_MULTIPLIER = 10;
  public static final int SOLO_BOSS__LONGEST_GEMLESS_STREAK = 5;
  
  public static final double LEVEL_EQUIP_BOOST_EXPONENT_BASE = 1.5;

  public static final double HEALTH__FORMULA_EXPONENT_BASE = 1.18;
  public static final double HEALTH__FORMULA_LINEAR_A = 2000;
  public static final double HEALTH__FORMULA_LINEAR_B = -62454;
  public static final int HEALTH__FORMULA_LEVEL_CUTOFF = 36;
  
  public static final int AVERAGE_SIZE_OF_LEVEL_BRACKET = 5;

  public static final int FORGE_MIN_DIAMOND_COST_FOR_GUARANTEE = 1;
  public static final double FORGE_DIAMOND_COST_FOR_GUARANTEE_EXPONENTIAL_MULTIPLIER = 2;
  public static final int FORGE_MAX_EQUIP_LEVEL = 10;
  public static final int FORGE_BASE_MINUTES_TO_ONE_GOLD = 15;
  public static final double FORGE_SPEEDUP_CONSTANT_A = 10.116;
  public static final double FORGE_SPEEDUP_CONSTANT_B = -32.59;
  public static final double FORGE_TIME_BASE_FOR_EXPONENTIAL_MULTIPLIER = 1.8;
  public static final int FORGE_DEFAULT_NUMBER_OF_FORGE_SLOTS = 1;
  public static final int FORGE__ADDITIONAL_MAX_FORGE_SLOTS = 2;
  public static final int FORGE_COST_OF_PURCHASING_SLOT_TWO = 250;
  public static final int FORGE_COST_OF_PURCHASING_SLOT_THREE = 650;

  public static final int EXPANSION_WAIT_COMPLETE__HOUR_CONSTANT = 0;
  public static final int EXPANSION_WAIT_COMPLETE__HOUR_INCREMENT_BASE = 4;
  public static final int EXPANSION_WAIT_COMPLETE__BASE_MINUTES_TO_ONE_GOLD = 3;

  public static final int PURCHASE_EXPANSION__COST_CONSTANT = 1000;
  public static final int PURCHASE_EXPANSION__COST_EXPONENT_BASE = 2;
  
  public static final int SIZE_OF_ATTACK_LIST = 20;

  public static final int BATTLE__MAX_NUM_TIMES_ATTACKED_BY_ONE_IN_PROTECTION_PERIOD = 25;
  public static final int BATTLE__HOURS_IN_ATTACKED_BY_ONE_PROTECTION_PERIOD = 2;
  public static final int BATTLE__MAX_LEVEL_TO_STEAL = 4;
  public static final int BATTLE__MAX_LEVEL_TO_STEAL_EPICS = 2;
  public static final int BATTLE__MAX_LEVEL_TO_STILL_GENERATE_BOTS = 10;
  //minimum level prestiged players have to be to see only real players in the attack list
  public static final int BATTLE__MIN_LEVEL_FOR_PRESTIGED_TO_SEE_NON_BOTS = 30;
  
  //--------------------------------------------------------------------------------------------------------------------------

  //TUTORIAL CONSTANTS
  public static final double CHARACTERS_ATTACK_DEFENSE_VARIABILITY = 0.67;
  public static final int TUTORIAL__ARCHER_INIT_ATTACK = 12; 
  public static final int TUTORIAL__ARCHER_INIT_DEFENSE = 12;
  public static final int TUTORIAL__MAGE_INIT_ATTACK = 14; 
  public static final int TUTORIAL__MAGE_INIT_DEFENSE = 10;
  public static final int TUTORIAL__WARRIOR_INIT_ATTACK = 10;
  public static final int TUTORIAL__WARRIOR_INIT_DEFENSE = 14;
  public static final int TUTORIAL__INIT_ENERGY = 50;
  public static final int TUTORIAL__INIT_STAMINA = 3;
  public static final int TUTORIAL__INIT_HEALTH = 30;
  //public static final int TUTORIAL__INIT_DIAMONDS = 20;
  public static final int TUTORIAL__INIT_COINS = 50;
  public static final int TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT = 2; //Because it does not warn the user
  public static final int TUTORIAL__ARCHER_INIT_WEAPON_ID = 1;
  public static final int TUTORIAL__ARCHER_INIT_ARMOR_ID = 41;
  public static final int TUTORIAL__MAGE_INIT_WEAPON_ID = 1;
  public static final int TUTORIAL__MAGE_INIT_ARMOR_ID = 41;
  public static final int TUTORIAL__WARRIOR_INIT_WEAPON_ID = 1;
  public static final int TUTORIAL__WARRIOR_INIT_ARMOR_ID = 41;
  public static final String TUTORIAL__FAKE_QUEST_GOOD_NAME = "Preserve the Peace";
  public static final String TUTORIAL__FAKE_QUEST_BAD_NAME = "Witness Protection";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_ACCEPT_DIALOGUE = "10~good~";
  public static final String TUTORIAL__FAKE_QUEST_BAD_ACCEPT_DIALOGUE = "10~bad~";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION = "Soldier, we are in dire times and we need your help.";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION = "Soldier, we are in dire times and we need your help.";
  public static final String TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE = "Simply amazing! Your battle prowess makes our village seem safer already. ";
  public static final String TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE = "Excellent work soldier. Good to know I have a competent ally watching my back.";
  public static final int TUTORIAL__FIRST_TASK_ID = 1;
  public static final int TUTORIAL__FAKE_QUEST_TASK_ID = 168;
  public static final int TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY = 0;
  public static final int TUTORIAL__FAKE_QUEST_COINS_GAINED = 8;
  public static final int TUTORIAL__FAKE_QUEST_EXP_GAINED = 4;
  public static final int TUTORIAL__FAKE_QUEST_AMULET_LOOT_EQUIP_ID = 250;
  public static final int TUTORIAL__FIRST_BATTLE_COIN_GAIN = 5;
  public static final int TUTORIAL__FIRST_BATTLE_EXP_GAIN = 1;
  public static final int TUTORIAL__FIRST_STRUCT_TO_BUILD = 1;
  public static final int TUTORIAL__FIRST_NEUTRAL_CITY_ID = 1;
  public static final int TUTORIAL__COST_TO_SPEED_UP_FORGE = 2;
  
  //STARTUP
  public static final int STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND = 20;
  public static final int STARTUP__HOURS_OF_BATTLE_NOTIFICATIONS_TO_SEND = 24*2;
  public static final int STARTUP__APPROX_NUM_ALLIES_TO_SEND = 20;
  public static final int STARTUP__DAILY_BONUS_MAX_CONSECUTIVE_DAYS = 5;
  //public static final int STARTUP__DAILY_BONUS_TIME_REQ_BETWEEN_CONSEC_DAYS = 1; //in days
  //public static final int STARTUP__DAILY_BONUS_SMALL_BONUS_COIN_QUANTITY = 2;
  //public static final int STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_SMALL_BONUS = 1;
  //public static final int STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_BIG_BONUS = 5;
  //public static final int STARTUP__DAILY_BONUS_MAX_CONSEC_DAYS_BIG_BONUS = 5;
  //public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_COMMON_EQUIP = 0.1;    //total should add up to 1
  //public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_UNCOMMON_EQUIP = 0.85;
  //public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_RARE_EQUIP = 0;
  //public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_EPIC_EQUIP = 0.05;
  //public static final double STARTUP__DAILY_BONUS_PERCENTAGE_CHANCE_LEGENDARY_EQUIP = 0;
  //public static final int STARTUP__DAILY_BONUS_RECEIVE_EQUIP_LEVEL_RANGE = 5;
  //public static final int STARTUP__DAILY_BONUS_MYSTERY_BOX_EQUIP_FORGE_LEVEL_MAX = 2;
  public static final int STARTUP__CLAN_HOUSE_MIN_LEVEL = 16;
  public static final int STARTUP__VAULT_MIN_LEVEL = 1;
  public static final int STARTUP__ARMORY_MIN_LEVEL = 1;
  public static final int STARTUP__MARKETPLACE_MIN_LEVEL = 7;
  public static final int STARTUP__BLACKSMITH_MIN_LEVEL = 1;
  public static final int STARTUP__LEADERBOARD_MIN_LEVEL = 1;
  public static final int STARTUP__ENHANCING_MIN_LEVEL_TO_UNLOCK = 20; 
  public static final boolean STARTUP__USE_OLD_BATTLE_FORMULA = true;
  public static final int STARTUP__ADMIN_CHAT_USER_ID = 98394;//Globals.IS_SANDBOX() ? 98437 : 131287;
  public static final int STARTUP__MAX_PRIVATE_CHAT_POSTS_SENT = 150;
  public static final int STARTUP__MAX_PRIVATE_CHAT_POSTS_RECEIVED = 150;
  //ARMORY
  public static final double ARMORY__SELL_RATIO = 0.15;
  
  //BATTLE
  public static final int BATTLE__MAX_ITEMS_USED = 4;   //unused right now
  public static final int BATTLE__MAX_LEVEL_DIFFERENCE = 3;
  public static final double BATTLE__A = .2;		//must be <= 1
  public static final double BATTLE__B = 80;
  public static final int BATTLE__MIN_COINS_FROM_WIN = 5;
  public static final double BATTLE__EXP_BASE_MULTIPLIER = 0.8;
  public static final int BATTLE__EXP_MIN = 1;
  public static final double BATTLE__EXP_LEVEL_DIFF_WEIGHT = 0.2;
  public static final double BATTLE__CHANCE_OF_EQUIP_LOOT_INITIAL_WALL = Globals.IS_SANDBOX() ? 0.5 : 0.15;
  public static final double BATTLE__EQUIP_AND_STATS_WEIGHT = 1.08;
  public static final double BATTLE__MIN_LEVEL_TO_NOT_DISPLAY_BOTS_IN_ATTACK_LIST = 30;

  //GENERATE ATTACK LIST
  public static final int GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX = 25;
  
  //POST TO MARKETPLACE
  public static final int POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER = 50;  
  public static final int POST_TO_MARKETPLACE__MAX_MILLISECOND_DELAY_ADDED_TO_POST_TIME = 1800000;//30*60*1000; //30 minutes
  public static final int POST_TO_MARKETPLACE__MIN_MILLISECOND_DELAY_ADDED_TO_POST_TIME =  600000;//10*60*1000; //10 minutes
  
  //PURCHASE FROM MARKETPLACE
  public static final double PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .30;
  
  //TASK ACTION
  public static final int TASK_ACTION__MAX_CITY_RANK = 5;
  //if dev server then always drop gem, else production go with whatever
  public static final float TASK_ACTION__GEM_DROP_RATE = Globals.IS_SANDBOX() ? 0.1f : 0.1f; 
  public static final int TASK_ACTION__MAX_ENERGY_COST_MULTIPLIER = 5;
  public static final int TASK_ACTION__MAX_CITY_RANK_UP_REWARD_MULTIPLIER = 6;
  
  //PURCHASE NORM STRUCTURE
  public static final int PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE = 3;

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
  public static final int RETRACT_MARKETPLACE_POST__MIN_NUM_DAYS_UNTIL_FREE_TO_RETRACT_ITEM = 7;
  
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
  
  //EARN FREE DIAMONDS
  public static final int EARN_FREE_DIAMONDS__NUM_VIDEOS_FOR_DIAMOND_REWARD = 20;
  public static final int EARN_FREE_DIAMONDS__FB_CONNECT_REWARD = 10;
  
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
  public static final int PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST = 50;
  public static final int PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST = 10;

  //USER CREATE 
  public static final int USER_CREATE__START_LEVEL = 2;
  public static final int USER_CREATE__MIN_NAME_LENGTH = 1;
  public static final int USER_CREATE__MAX_NAME_LENGTH = 15;
  public static final int USER_CREATE__MIN_COIN_REWARD_FOR_REFERRER = 100;
  public static final int USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED = 50;
  public static final double USER_CREATE__PERCENTAGE_OF_COIN_WEALTH_GIVEN_TO_REFERRER = .2;
  public static final int USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL = 98394;
  public static final String USER_CREATE__FIRST_WALL_POST_TEXT = 
	  "Hi! My name's " + (Globals.KABAM_ENABLED() ? "Stevie" : "Andrew") + ", one of the creators of this game. Feel free to message me if you need any help.";
  public static final int USER_CREATE__INITIAL_GLOBAL_CHATS = 10;

  
  //LEVEL UP
  public static final int LEVEL_UP__SKILL_POINTS_GAINED = 3;
  public static final int LEVEL_UP__MAX_LEVEL_FOR_USER = 100; //add level up equipment for fake players if increasing
//  public static final double LEVEL_UP_HEALTH_GAINED = 5.0;
  public static final double LEVEL_UP_ATTACK_GAINED = 2.0;
  public static final double LEVEL_UP_DEFENSE_GAINED = 2.0;
  
  //LEVEL UP EQUIPMENT FOR FAKE PLAYERS (levels 1-30 must add more if going above level 30)
  public static final int[] ALL_CHARACTERS_WEAPON_ID_PER_LEVEL = 
    {1,1,1,1,1,          1,124,6,11,121,
    3,7,121,131,197,     201,137,19,19,139,
    24,144,210,210,27,   150,216,216,152,153,
    153,153,153,153,153, 153,153,153,153,153,
    316,316,316,316,316, 316,316,316,316,316,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    };
  public static final int[] ALL_CHARACTERS_ARMOR_ID_PER_LEVEL = 
    {41,41,41,41,41,     41,160,160,162,162,
    226,226,165,45,45,  169,169,169,67,67,
    174,174,72,72,72,    72,72,245,245,245,
    245,245,245,245,245, 245,245,245,245,245,
    309,309,309,309,309, 309,309,309,309,309,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    };
  /*//rendered useless by booster pack feature
  public static final int[] WARRIOR_WEAPON_ID_LEVEL = 
    {1,1,1,3,3, 5,5,6,6,7, 
    11,11,12,12,13, 17,18,19,19,20, 
    24,25,26,26,27, 31,32,32,33,34,
    34,34,34,34,34, 34,34,34,34,34,
    287,287,287,287,287, 287,287,287,287,287,
    365,365,365,365,365, 365,365,365,365,365,
    365,365,365,365,365, 365,365,365,365,365,
    365,365,365,365,365, 365,365,365,365,365,
    365,365,365,365,365, 365,365,365,365,365,
    365,365,365,365,365, 365,365,365,365,365};
  public static final int[] WARRIOR_ARMOR_ID_LEVEL = 
    {41,41,41,43,43, 44,44,45,45,45, 
    47,47,47,48,48, 51,51,51,67,67, 
    71,71,72,72,72, 76,76,76,77,77,
    77,77,77,77,77, 77,77,77,77,77,
    292,292,292,292,292, 292,292,292,292,292,
    370,370,370,370,370, 370,370,370,370,370,
    370,370,370,370,370, 370,370,370,370,370,
    370,370,370,370,370, 370,370,370,370,370,
    370,370,370,370,370, 370,370,370,370,370,
    370,370,370,370,370, 370,370,370,370,370};
  public static final int[] ARCHER_WEAPON_ID_LEVEL = 
    {120,120,120,122,122, 124,124,125,126,126,
    130,130,131,131,132, 136,137,138,138,139, 
    143,144,144,145,146, 150,151,151,152,153,         
    153,153,153,153,153, 153,153,153,153,153,
    304,304,304,304,304, 304,304,304,304,304,
    381,381,381,381,381, 381,381,381,381,381,
    381,381,381,381,381, 381,381,381,381,381,
    381,381,381,381,381, 381,381,381,381,381,
    381,381,381,381,381, 381,381,381,381,381,
    381,381,381,381,381, 381,381,381,381,381};
  public static final int[] ARCHER_ARMOR_ID_LEVEL = 
    {159,159,159,161,161, 162,162,162,163,163, 
    165,165,165,166,166, 169,169,169,170,170, 
    174,174,175,175,175, 179,179,179,180,180,      
    180,180,180,180,180, 180,180,180,180,180,
    309,309,309,309,309, 309,309,309,309,309,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386,
    386,386,386,386,386, 386,386,386,386,386};
  public static final int[] MAGE_WEAPON_ID_LEVEL = 
    {185,185,185,187,187, 189,189,190,191,191, 
    195,195,196,196,197, 201,202,203,203,204, 
    208,209,209,210,211, 215,216,216,217,218,      
    218,218,218,218,218, 218,218,218,218,218,
    316,316,316,316,316, 316,316,316,316,316,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392,
    392,392,392,392,392, 392,392,392,392,392};
  public static final int[] MAGE_ARMOR_ID_LEVEL = 
    {224,224,224,226,226, 227,227,227,228,228, 
    230,230,230,231,231, 234,234,234,235,235, 
    239,239,240,240,240, 244,244,244,245,245,      
    245,245,245,245,245, 245,245,245,245,245,
    321,321,321,321,321, 321,321,321,321,321,
    397,397,397,397,397, 397,397,397,397,397,
    397,397,397,397,397, 397,397,397,397,397,
    397,397,397,397,397, 397,397,397,397,397,
    397,397,397,397,397, 397,397,397,397,397,
    397,397,397,397,397, 397,397,397,397,397};*/
  
  public static final int[] ALL_CHARACTERS_EQUIP_LEVEL = 
    {250,250,250,250,250, 250,251,251,254,254,
    255,252,252,256,256, 256,259,259,260,260,
    264,264,265,265,261, 261,261,266,266,266, 
    270,270,271,271,272, 276,276,276,277,277,
    277,277,277,277,277, 277,277,277,277,277,
    297,297,297,297,297, 297,297,297,297,297,
    375,375,375,375,375, 375,375,375,375,375,
    375,375,375,375,375, 375,375,375,375,375,
    375,375,375,375,375, 375,375,375,375,375,
    375,375,375,375,375, 375,375,375,375,375};
  
  //POST_ON_PLAYER_WALL
  public static final int POST_ON_PLAYER_WALL__MAX_CHAR_LENGTH = 1000;

  //RETRIEVE PLAYER WALL POSTS
  public static final int RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP = 100;
  
  //CHARACTER MOD
  public static final int CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_CHARACTER_TYPE = 150;
  public static final int CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_NAME = 50;
  public static final int CHARACTER_MOD__DIAMOND_COST_OF_NEW_PLAYER = 0;
  public static final int CHARACTER_MOD__DIAMOND_COST_OF_RESET_SKILL_POINTS = 100;

  //LEADERBOARD
  public static final int LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION = 100;
  public static final int LEADERBOARD__MAX_PLAYERS_SENT_AT_ONCE = 15;
  public static final int LEADERBOARD_EVENT__MAX_PLAYERS_SENT_AT_ONCE = 200;
  
  //SEND GROUP CHAT
  public static final int SEND_GROUP_CHAT__MAX_LENGTH_OF_CHAT_STRING = 200;
  
  //PURCHASE GROUP CHAT
  public static final int PURCHASE_GROUP_CHAT__NUM_CHATS_GIVEN_FOR_PACKAGE = 100;
  public static final int PURCHASE_GROUP_CHAT__DIAMOND_PRICE_FOR_PACKAGE = 10;
  
  //CREATE CLAN
  public static final int CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN = 100;
  public static final int CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME = 15;
  public static final int CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION = 350;
  public static final int CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_TAG = 5;
  public static final int RETRIEVE_CLANS__NUM_CLANS_CAP = 50;
  public static final int CREATE_CLAN__INITIAL_CLAN_LEVEL = 1;
  public static final int CLAN__ALLIANCE_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT = Globals.IS_SANDBOX() ? 967 : 92;
  public static final int CLAN__LEGION_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT = Globals.IS_SANDBOX() ? 958 : 148;
  public static final int CLAN__ALLIANCE_LEGION_LIMIT_TO_RETRIEVE_FROM_DB = 50;
  
  //THREE CARD MONTE
  public static final int THREE_CARD_MONTE__DIAMOND_PRICE_TO_PLAY = 10;
  public static final int THREE_CARD_MONTE__MIN_LEVEL = 5;
  public static final float THREE_CARD_MONTE__BAD_PERCENTAGE = 0.63f;
  public static final float THREE_CARD_MONTE__MEDIUM_PERCENTAGE = 0.35f;
  public static final float THREE_CARD_MONTE__GOOD_PERCENTAGE = 0.02f;
  
  //GOLDMINE
  public static final int GOLDMINE__NUM_HOURS_BEFORE_RETRIEVAL = 11;
  public static final int GOLDMINE__NUM_HOURS_TO_PICK_UP = 1;
  public static final int GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP = 1;
  public static final int GOLDMINE__GOLD_COST_TO_RESTART = 10;
  
  //LOCK BOXES
  public static final int LOCK_BOXES__GOLD_COST_TO_PICK = 10;
  public static final int LOCK_BOXES__SILVER_COST_TO_PICK = 500;
  public static final float LOCK_BOXES__GOLD_CHANCE_TO_PICK = 1.f;
  public static final float LOCK_BOXES__SILVER_CHANCE_TO_PICK = 0.25f;
  public static final float LOCK_BOXES__FREE_CHANCE_TO_PICK = 0.15f;
  public static final int LOCK_BOXES__NUM_MINUTES_TO_REPICK = 60;
  public static final int LOCK_BOXES__GOLD_COST_TO_RESET_PICK = 10;
  public static final float LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_TASK_BASE = 0.03f;
  public static final float LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_TASK_MAX = 1.f;
  public static final float LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_BATTLE = 0.25f;
  public static final int LOCK_BOXES__NUM_DAYS_AFTER_END_DATE_TO_KEEP_SENDING_PROTOS = 3;
  
  //TIME BEFORE RESHOWING MENUS
  public static final int NUM_HOURS_BEFORE_RESHOWING_GOLD_SALE = 24;
  public static final int NUM_HOURS_BEFORE_RESHOWING_LOCK_BOX = 24;
  public static final int NUM_HOURS_BEFORE_RESHOWING_BOSS_EVENT = 24;
  public static final int LEVEL_TO_SHOW_RATE_US_POPUP = 8;
  
  // GOLD SALE NEW USERS
  public static final int NUM_DAYS_FOR_NEW_USER_GOLD_SALE = 3;
  public static final String GOLD_SHOPPE_IMAGE_NAME_NEW_USER_GOLD_SALE = "BeginnerSaleSign.png";
  public static final String GOLD_BAR_IMAGE_NAME_NEW_USER_GOLD_SALE = "BeginnerSale.png";
  public static final int NUM_BEGINNER_SALES_ALLOWED = 2;
  
  //CLAN TOWER
  public static final int MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER = Globals.IS_SANDBOX() ? 2 : 25;
  public static final int NUM_HOURS_BEFORE_REWAGING_WAR_ON_TOWER = 6;
  public static final int CLAN_TOWER__MAX_NUM_TOWERS_CLAN_CAN_HOLD = 1;
  
  //LEADERBOARD EVENT
  public static final int LEADERBOARD_EVENT__WINS_WEIGHT = 2;
  public static final int LEADERBOARD_EVENT__LOSSES_WEIGHT = -1;
  public static final int LEADERBOARD_EVENT__FLEES_WEIGHT = -3;
  public static final int LEADERBOARD_EVENT__NUM_HOURS_TO_SHOW_AFTER_EVENT_END = 24;
  
  //USER CURRENCY HISTORY REASON FOR CHANGE VALUES
  public static final String UCHRFC__USER_CREATED = "user created";
  public static final String UCHRFC__LEADERBOARD = "leaderboard event";
  public static final String UCHRFC__CLAN_TOWER_WAR_ENDED = "clan tower war ended";
  public static final String UCHRFC__SHORT_MARKET_PLACE_LICENSE = "purchased short market place license";
  public static final String UCHRFC__LONG_MARKET_PLACE_LICENSE = "purchased long market place license";
  public static final String UCHRFC__GROUP_CHAT = "purchased group chat"; //is controller for this even used?
  public static final String UCHRFC__BOSS_ACTION = "boss action";
  public static final String UCHRFC__REFILL_STAT = "refilled stat: ";
  public static final String UCHRFC__FINISH_NORM_STRUCT = "finish norm stuct: ";
  public static final String UCHRFC__UPGRADE_NORM_STRUCT = "upgraded norm struct";
  public static final String UCHRFC__SELL_NORM_STRUCT = "sell norm struct";
  public static final String UCHRFC__PURCHASE_NORM_STRUCT = "purchased norm struct";
  public static final String UCHRFC__QUEST_REDEEM = "quest redeemed";
  public static final String UCHRFC__REDEEM_MARKETPLACE_EARNINGS = "redeemed marketplace earnings";
  public static final String UCHRFC__PICK_LOCKBOX = "picked lockbox";
  public static final String UCHRFC__RETRACT_MARKETPLACE_POST = "retract marketplace post";
  public static final String UCHRFC__PLAY_THREE_CARD_MONTE = "played three card monte";
  //public static final String UCHRFC__SOLD_ITEM_ON_MARKETPLACE = "sold item on marketplace"; //user's currency change is 0
  public static final String UCHRFC__PURCHASED_FROM_MARKETPLACE = "purchased from marketplace";
  public static final String UCHRFC__EXPANSION_WAIT_COMPLETE = "expansion wait complete: ";
  public static final String UCHRFC__SUBMIT_EQUIPS_TO_BLACKSMITH = "submit equips to blacksmith";
  public static final String UCHRFC__FINISH_FORGE_ATTEMPT_WAIT_TIME = "finish forge attempt wait time";
  public static final String UCHRFC__IN_APP_PURCHASE = "inapp purchase: ";
  public static final String UCHRFC__ARMORY_TRANSACTION = "armory transaction";
  public static final String UCHRFC__UPGRADE_CLAN_TIER_LEVEL = "upgraded clan tier level";
  public static final String UCHRFC__CREATE_CLAN = "created clan";
  public static final String UCHRFC__EARN_FREE_DIAMONDS_KIIP = "kiip";
  public static final String UCHRFC__EARN_FREE_DIAMONDS_ADCOLONY = "adcolony";
  public static final String UCHRFC__EARN_FREE_DIAMONDS_FB_CONNECT = "connecting to facebook";
  public static final String UCHRFC__CHARACTER_MOD_TYPE = "character type, class";
  public static final String UCHRFC__CHARACTER_MOD_NAME = "character name";
  public static final String UCHRFC__CHARACTER_MOD_RESET = "character reset";
  public static final String UCHRFC__CHARACTER_MOD_SKILL_POINTS = "character skill points";
  public static final String UCHRFC__GOLDMINE = "goldmine reset";
  public static final String UCHRFC__COLLECT_GOLDMINE = "collect from goldmine";
  public static final String UCHRFC__SPED_UP_ENHANCING = "sped up enhancing ";
  public static final String UCHRFC__PURHCASED_BOOSTER_PACK = "purchased booster pack with id ";
  public static final String UCHRFC__PURCHASED_ADDITIONAL_FORGE_SLOTS = "purchased additional forge slots";
  //silver only reasons
  public static final String UCHRFC__RETRIEVE_CURRENCY_FROM_NORM_STRUCT = "retrieve currency from normal structures";
  public static final String UCHRFC__TASK_ACTION = "performed task with id ";
  public static final String UCHRFC__STARTUP_DAILY_BONUS = "startup daily bonus";
  public static final String UCHRFC__PURCHASE_CITY_EXPANSION = "expanded city: ";
  public static final String UCHRFC__USER_CREATE_REFERRED_A_USER = "referred a user";
  public static final String UCHRFC__VAULT_DEPOSIT = "vault deposit";
  public static final String UCHRFC__BATTLE_WON = "won battle";
  public static final String UCHRFC__BATTLE_LOST = "lost battle";
  
  //ENHANCING
  public static final int MAX_ENHANCEMENT_LEVEL = 5;
  public static final int ENHANCEMENT__PERCENTAGE_PER_LEVEL = 10000;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_A = 0.f;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_B = 0.f;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_C = 1;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_D = 0.1f;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_E = 1.5f;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_F = 2.1f;
  public static final float ENHANCEMENT__TIME_FORMULA_CONSTANT_G = 1.365f;
  public static final float ENHANCEMENT__PERCENT_FORMULA_CONSTANT_A = 0.75f;
  public static final float ENHANCEMENT__PERCENT_FORMULA_CONSTANT_B = 2.f;
  public static final float ENHANCEMENT__ENHANCE_LEVEL_EXPONENT_BASE = 1.2f;
  public static final int ENHANCEMENT__DEFAULT_SECONDS_TO_ENHANCE = 5;
  
  //BOOSTER PACKS
  //amount of booster packs user can buy at one time
  public static final int BOOSTER_PACK__PURCHASE_OPTION_ONE_NUM_BOOSTER_ITEMS = 1;
  public static final int BOOSTER_PACK__PURCHASE_OPTION_TWO_NUM_BOOSTER_ITEMS = 10;
  public static final String BOOSTER_PACK__INFO_IMAGE_NAME = "howchestswork.png";
  public static final int BOOSTER_PACK__NUM_TIMES_TO_BUY_STARTER_PACK = 4;
  public static final int BOOSTER_PACK__NUM_DAYS_TO_BUY_STARTER_PACK = 3;
  
  //MENTORING
  public static final int MENTORSHIPS__MAX_MENTEE_LIMIT = 25;
  //mentor has to wait some minutes before they can acquire another user to mentor
  public static final int MENTORSHIPS__MINUTES_UNTIL_NEXT_MENTORSHIP = 30; 
  public static final String MENTORSHIPS_INITIAL_MESSAGE_ONE = "Hi %@, I am a mentor in age of chaos! I help new players learn the ropes";
  public static final String MENTORSHIPS_INITIAL_MESSAGE_TWO = "how are things?";
  public static final int MENTORSHIPS__MAX_LEVEL_LIMIT_TO_STILL_BE_A_MENTEE = 60;
  public static final int MENTORSHIPS__SUBTRAHEND_IN_MINUTES_TO_NOW_TO_FIND_MENTEE = 60;
  public static final int MENTORSHIPS__MAX_MENTEES_TO_RETRIEVE = 100;
  public static final int MENTORSHIPS__MENTEE_LEVEL_FOR_QUEST = 15;
  public static final int MENTORSHIPS__MENTEE_LEVEL_FOR_SECOND_QUEST = 30;
  public static final int MENTORSHIPS__MENTEE_EQUIP_FORGE_LEVEL_FOR_QUEST = 3;
  
  
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
    new AnimatedSpriteOffset("TutorialGuide", new CoordinatePair(0, -5)),
    new AnimatedSpriteOffset("TutorialGuideBad", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("AllianceArcher", new CoordinatePair(0, -5)),
    new AnimatedSpriteOffset("AllianceWarrior", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("AllianceMage", new CoordinatePair(0, -6)),
    new AnimatedSpriteOffset("LegionArcher", new CoordinatePair(0, -7)),
    new AnimatedSpriteOffset("LegionWarrior", new CoordinatePair(0, -11)),
    new AnimatedSpriteOffset("LegionMage", new CoordinatePair(0, -8)),
    new AnimatedSpriteOffset("Bandit", new CoordinatePair(0, -15)),
    new AnimatedSpriteOffset("FarmerMitch", new CoordinatePair(0, -8)),
    new AnimatedSpriteOffset("Carpenter", new CoordinatePair(0, -6)),
  };
  
  public static final String[] STARTUP__NOTICES_TO_PLAYERS = {
//    "FREE limited edition gold equip for joining today!"
//    "Forging Contest! 50 GOLD reward! Details at forum.lvl6.com"
//      "We have just added 40+ equips, a new city, and increased the level cap!"
      "Please look at your private chats for an important message from Oishii."
    };
  
  
  public static final int[] STARTUP__LEVELS_THAT_TRIGGER_KIIP_REWARDS = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 
    19, 20, 21, 23, 24, 25, 26};
  public static final int[] STARTUP__QUESTS_THAT_TRIGGER_KIIP_REWARDS_ON_REDEEM = {};
  public static final int STARTUP__QUEST_ID_FOR_FIRST_LOSS_TUTORIAL = 326;
  public static final int[] STARTUP__QUEST_IDS_FOR_GUARANTEED_WIN = {325};
  public static final String STARTUP__FAQ_FILE_NAME = "FAQ.3.txt";
  public static final String STARTUP__PRESTIGE_FAQ_FILE_NAME = "PrestigeFAQ.txt";
  public static final int STARTUP__DEFAULT_DAYS_BATTLE_SHIELD_IS_ACTIVE = 7;
  public static final float CHANCE_TO_GET_KIIP_ON_BATTLE_WIN = 1.f;
  public static final float CHANCE_TO_GET_KIIP_ON_QUEST_REDEEM = 1.f;

  public static final String NIB_NAME__THREE_CARD_MONTE = "ThreeCardMonte.4";
  public static final String NIB_NAME__LOCK_BOX = "LockBox.4";
  public static final String NIB_NAME__TRAVELING_MAP = "TravelingMap.3";
  public static final String NIB_NAME__GOLD_MINE = "GoldMine.2";
  public static final String NIB_NAME__EXPANSION = "Expansion.2";
  public static final String NIB_NAME__MARKET_FILTERS = "MarketplaceFilters.3";
  public static final String NIB_NAME__BLACKSMITH = "Blacksmith.6";
  public static final String NIB_NAME__GOLD_SHOPPE = "GoldShoppe.4";
  public static final String NIB_NAME__BOSS_EVENT = "BossEvent.2";
  public static final String NIB_NAME__DAILY_BONUS = "DailyBonus.1";
  
  public static final int IDDICTION__EQUIP_ID = 282;
  public static final String IDDICTION__NOTICE = "FREE limited edition gold equip for joining today!";
  
  //prestige
  public static final int PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_WEAPON = 1;
  public static final int PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_ARMOR = 2;
  public static final int PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_AMULET = 3;
  public static final int PRESTIGE__MIN_LEVEL_FOR_PRESTIGE = 60;
  public static final int PRESTIGE__MAX_PRESTIGE_LEVEL = 3;

}
