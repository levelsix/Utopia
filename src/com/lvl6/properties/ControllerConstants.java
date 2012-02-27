package com.lvl6.properties;

public class ControllerConstants {

  public static final int NOT_SET = -1;
  
  //LEVEL UP
  public static final int MAX_LEVEL_FOR_USER = 300;
  
  //GENERATING LISTS OF ENEMIES
  public static final int NUM_MINUTES_SINCE_LAST_BATTLE_BEFORE_APPEARANCE_IN_ATTACK_LISTS = 20;
  
  //CRIT STRUCTS
  public static final int LUMBERJACK_DEFAULT_X = 3;
  public static final int LUMBERJACK_DEFAULT_Y = 3;
  public static final int CARPENTER_DEFAULT_X = 3;
  public static final int CARPENTER_DEFAULT_Y = 3;
  public static final int AVIARY_DEFAULT_X = 3;
  public static final int AVIARY_DEFAULT_Y = 3;
  
  public static final int ARMORY_XLENGTH = 3;
  public static final int ARMORY_YLENGTH = 3;
  public static final int VAULT_XLENGTH = 3;
  public static final int VAULT_YLENGTH = 3;
  public static final int MARKETPLACE_XLENGTH = 3;
  public static final int MARKETPLACE_YLENGTH = 3;
  public static final int LUMBERJACK_XLENGTH = 3;
  public static final int LUMBERJACK_YLENGTH = 3;
  public static final int CARPENTER_XLENGTH = 3;
  public static final int CARPENTER_YLENGTH = 3;
  public static final int AVIARY_XLENGTH = 3;
  public static final int AVIARY_YLENGTH = 3;
  
  
  //ARMORY
  public static final double ARMORY__SELL_RATIO = 0.5;
  
  
  //BATTLE
  public static final int BATTLE__MAX_ITEMS_USED = 4;
  public static final int BATTLE__MAX_DAMAGE = 24;
  public static final int BATTLE__MIN_DAMAGE_DEALT_TO_LOSER = BATTLE__MAX_DAMAGE - 10;
  public static final int BATTLE__MAX_LEVEL_DIFFERENCE = 50;
  public static final int BATTLE__MIN_BATTLE_LEVEL = 3;
  public static final int BATTLE__MIN_EXP_GAIN = 1;
  public static final int BATTLE__MAX_EXP_GAIN = 5;
    
  /* FORMULA FOR CALCULATING PLAYER'S BATTLE STAT
  Let S = Attack or Defense skill points, based on whether the user is the attacker or defender
  Let I = The total attack/defense of the items used in the battle, based on whether the user is the attacker or defender
  Let A = The user’s agency size
  Let F = The final combined stat (attack or defense)
  Then F = RAND(X * (S + I / Z), Y * (S + I / Z))
  To put it into words, we take (skill points times agency size) and add (total item stats divided by Z), and then multiply by X and Y and return a random number between those two totals.
  Note that the S and I values are already passed into the computeStat() function and the function should return F. Note also that A (agency size) should be passed into computeStat() so the function header needs to be adjusted, as do the two calls to computeStat() in backend/attackplayer.php.
   */
  public static final double BATTLE__X = .8;
  public static final double BATTLE__Y = 1.2;
  public static final double BATTLE__Z = 4;
  
  /* FORMULA FOR CALCULATING COIN TRANSFER
   * (int) Math.rint(Math.min(loser.getCoins() * (Math.random()+1)/A, loser.getLevel()*B)); 
   */
  public static final double BATTLE__A = 10;
  public static final double BATTLE__B = 75000;
  
  
  //CLERIC HEAL
  //Formula: (int) Math.ceil(Math.pow(user.getLevel(), A)*healthToHeal*B)
  public static final double CLERIC_HEAL__A = 3;
  public static final double CLERIC_HEAL__B = .05;

  
  //GENERATE ATTACK LIST
  public static final int GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX = 25;
  public static final int GENERATE_ATTACK_LIST__LONGITUDE_MIN = -180;
  public static final int GENERATE_ATTACK_LIST__LONGITUDE_MAX = 180;
  public static final int GENERATE_ATTACK_LIST__LATITUDE_MIN = -90;
  public static final int GENERATE_ATTACK_LIST__LATITUDE_MAX = 90;

  
  //IAP
  public static final boolean IN_APP_PURCHASE__IS_SANDBOX = false;
  
  
  //POST TO MARKETPLACE
  public static final int POST_TO_MARKETPLACE__MAX_MARKETPLACE_POSTS_FROM_USER = 15;  

  
  //PURCHASE FROM MARKETPLACE
  public static final double PURCHASE_FROM_MARKETPLACE__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .20;

  
  //TASK ACTION
  public static final int TASK_ACTION__MAX_CITY_RANK = 3;
  
  //PURCHASE NORM STRUCTURE
  public static final int PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE = 2;
  
  //SELL NORM STRUCTURE
  public static final double SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER = .3;

  
  //RETRIEVE CURRENT MARKETPLACE POSTS
  public static final int RETRIEVE_CURRENT_MARKETPLACE_POSTS__NUM_POSTS_CAP = 100;

  
  //RETRACT MARKETPLACE POST
  public static final double RETRACT_MARKETPLACE_POST__PERCENT_CUT_OF_SELLING_PRICE_TAKEN = .1;
  
  
  //USE SKILL POINT
  public static final int USE_SKILL_POINT__ATTACK_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__HEALTH_BASE_GAIN = 10;
  public static final int USE_SKILL_POINT__STAMINA_BASE_GAIN = 1;
  public static final int USE_SKILL_POINT__ATTACK_BASE_COST = 1;
  public static final int USE_SKILL_POINT__DEFENSE_BASE_COST = 1;
  public static final int USE_SKILL_POINT__ENERGY_BASE_COST = 1;
  public static final int USE_SKILL_POINT__HEALTH_BASE_COST = 1;
  public static final int USE_SKILL_POINT__STAMINA_BASE_COST = 2;

  
  //VAULT
  public static final int VAULT__DEPOSIT_PERCENT_CUT = 10;
  
  
  //REFILL STAT WITH DIAMONDS
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL = 10;
  public static final int REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL = 10;
  
  
  //FINISH NORM STRUCT BUILD WITH DIAMONDS
  public static final int FINISH_NORM_STRUCT_BUILD__DIAMOND_COST_FOR_FINISH_NORM_STRUCT_BUILD = 10;
  public static final int FINISH_NORM_STRUCT_BUILD__DIAMOND_COST_FOR_FINISH_NORM_STRUCT_INCOME_WAIT = 10;
  
  
  //PLACE CRIT STRUCT
  public static final double PLACE_CRITSTRUCT__MIN_LEVEL_ARMORY = 3;
  public static final double PLACE_CRITSTRUCT__MIN_LEVEL_VAULT = 4;
  public static final double PLACE_CRITSTRUCT__MIN_LEVEL_MARKETPLACE = 5;

  
  //LOAD PLAYER CITY
  public static final int LOAD_PLAYER_CITY__APPROX_NUM_ALLIES_IN_CITY = 4;
  
  
  //EXPANSION WAIT COMPLETE
  public static final int EXPANSION_WAIT_COMPLETE__MINUTES_FOR_EXPANSION = 720;
  
  
  //REFILL STAT WAIT COMPLETE
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA = 3;
  public static final int REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY = 4;

  //PURCHASE MARKETPLACE LICENSE
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_LONG_LICENSE = 30;
  public static final int PURCHASE_MARKETPLACE_LICENSE__DAYS_FOR_SHORT_LICENSE = 3;
  public static final int PURCHASE_MARKETPLACE_LICENSE__LONG_DIAMOND_COST = 50;
  public static final int PURCHASE_MARKETPLACE_LICENSE__SHORT_DIAMOND_COST = 30;
}
