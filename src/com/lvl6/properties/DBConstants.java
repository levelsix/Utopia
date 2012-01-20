package com.lvl6.properties;

public class DBConstants {

  /* TABLENAMES*/
  public static final String TABLE_USER = "users";
  public static final String TABLE_USER_EQUIP = "user_equip";
  public static final String TABLE_USER_TASKS = "user_tasks";
  public static final String TABLE_USER_CITIES = "user_cities";
  public static final String TABLE_EQUIPMENT = "equipment";
  public static final String TABLE_TASKS = "tasks";
  public static final String TABLE_TASKS_EQUIPREQS = "tasks_equipreqs";
  public static final String TABLE_CITIES = "cities";
  public static final String TABLE_IAP_HISTORY = "iap_history";
  public static final String TABLE_MARKETPLACE = "marketplace";
  public static final String TABLE_MARKETPLACE_TRANSACTION_HISTORY = "marketplace_transaction_history";

  
  /*COLUMNNAMES*/
  public static final String GENERIC__USER_ID = "user_id";
  public static final String GENERIC__ID = "id";

  /*USER TABLE*/
  public static final String USER__ID = GENERIC__ID;
  public static final String USER__NAME = "name";
  public static final String USER__LEVEL = "level";
  public static final String USER__TYPE = "type";
  public static final String USER__ATTACK = "attack";
  public static final String USER__DEFENSE = "defense";
  public static final String USER__STAMINA = "stamina";
  public static final String USER__ENERGY = "energy";
  public static final String USER__HEALTH = "health";
  public static final String USER__SKILL_POINTS = "skill_points";
  public static final String USER__HEALTH_MAX = "health_max";
  public static final String USER__ENERGY_MAX = "energy_max";
  public static final String USER__STAMINA_MAX = "stamina_max";
  public static final String USER__DIAMONDS = "diamonds";
  public static final String USER__COINS = "coins";
  public static final String USER__WOOD = "wood";
  public static final String USER__VAULT_BALANCE = "vault_balance";
  public static final String USER__EXPERIENCE = "experience";
  public static final String USER__TASKS_COMPLETED = "tasks_completed";
  public static final String USER__BATTLES_WON = "battles_won";
  public static final String USER__BATTLES_LOST = "battles_lost";
  
  public static final String USER__UDID = "udid";
  
  
  /*USER EQUIP TABLE*/
  public static final String USER_EQUIP__USER_ID = GENERIC__USER_ID;
  public static final String USER_EQUIP__EQUIP_ID = "equip_id";
  public static final String USER_EQUIP__QUANTITY = "quantity";
  public static final String USER_EQUIP__IS_STOLEN = "is_stolen";

  /*EQUIPMENT TABLE*/
  public static final String EQUIPMENT__ID = GENERIC__ID;

  /*STRUCTURE TABLE*/
  public static final String STRUCTURE__ID = GENERIC__ID;

  /*USER TASK TABLE*/
  public static final String USER_TASK__USER_ID = GENERIC__USER_ID;
  public static final String USER_TASK__TASK_ID = "task_id";
  public static final String USER_TASK__NUM_TIMES_ACTED_IN_RANK = "num_times_acted_in_rank";
  
  /*USER CITY TABLE*/
  public static final String USER_CITIES__USER_ID = GENERIC__USER_ID;
  public static final String USER_CITIES__CITY_ID = "city_id";
  public static final String USER_CITIES__CURRENT_RANK = "current_rank";
  
  /*IAP TABLE*/
  public static final String IAP_HISTORY__ID = GENERIC__ID;
  public static final String IAP_HISTORY__USER_ID = GENERIC__USER_ID;
  public static final String IAP_HISTORY__TRANSACTION_ID = "transaction_id";
  public static final String IAP_HISTORY__PURCHASE_DATE = "purchase_date";
  public static final String IAP_HISTORY__PREMIUMCUR_PURCHASED = "premiumcur_purchased";
  public static final String IAP_HISTORY__CASH_SPENT = "cash_spent";
  public static final String IAP_HISTORY__UDID = "udid";
  public static final String IAP_HISTORY__PRODUCT_ID = "product_id";
  public static final String IAP_HISTORY__QUANTITY = "quantity";
  public static final String IAP_HISTORY__BID = "bid";
  public static final String IAP_HISTORY__BVRS = "bvrs";
  public static final String IAP_HISTORY__APP_ITEM_ID = "app_item_id";
  
  /*MARKETPLACE TABLE*/
  public static final String MARKETPLACE__ID = GENERIC__ID;
  public static final String MARKETPLACE__POSTER_ID = "poster_id";
  public static final String MARKETPLACE__POST_TYPE = "post_type";
  public static final String MARKETPLACE__TIME_OF_POST = "time_of_post";
  public static final String MARKETPLACE__POSTED_EQUIP_ID = "posted_equip_id";
  public static final String MARKETPLACE__POSTED_EQUIP_QUANTITY = "posted_equip_quantity";
  public static final String MARKETPLACE__POSTED_WOOD = "posted_wood";
  public static final String MARKETPLACE__POSTED_DIAMONDS = "posted_diamonds"; 
  public static final String MARKETPLACE__POSTED_COINS = "posted_coins";
  public static final String MARKETPLACE__DIAMOND_COST = "diamond_cost";
  public static final String MARKETPLACE__COIN_COST = "coin_cost";
  public static final String MARKETPLACE__WOOD_COST = "wood_cost";
  
  /*MARKETPLACE HISTORY TABLE*/
  public static final String MARKETPLACE_TRANSACTION_HISTORY__ID = GENERIC__ID;
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID = "poster_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__BUYER_ID = "buyer_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POST_TYPE = "post_type";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_POST = "time_of_post";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_PURCHASE = "time_of_purchase";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_ID = "posted_equip_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_QUANTITY = "posted_equip_quantity";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_WOOD = "posted_wood";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_DIAMONDS = "posted_diamonds"; 
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_COINS = "posted_coins";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__DIAMOND_COST = "diamond_cost";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__COIN_COST = "coin_cost";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__WOOD_COST = "wood_cost";
}
