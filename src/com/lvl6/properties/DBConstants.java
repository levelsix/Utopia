package com.lvl6.properties;

public class DBConstants {

  /* TABLENAMES*/
  public static final String TABLE_USER = "users";
  public static final String TABLE_USER_EQUIP = "user_equip";
  public static final String TABLE_USER_TASKS = "user_tasks";
  public static final String TABLE_USER_CITIES = "user_cities";
  public static final String TABLE_USER_QUESTS = "user_quests";
  public static final String TABLE_USER_STRUCTS = "user_structs";
  public static final String TABLE_USER_CRITSTRUCTS = "user_critstructs";
  public static final String TABLE_USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS = "user_quests_completed_defeat_type_jobs";
  public static final String TABLE_USER_QUESTS_COMPLETED_MARKETPLACE_JOBS = "user_quests_completed_marketplace_jobs";
  public static final String TABLE_USER_QUESTS_COMPLETED_TASKS = "user_quests_completed_tasks";
  public static final String TABLE_USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS = "user_quests_defeat_type_job_progress";
  public static final String TABLE_USER_QUESTS_MARKETPLACE_JOB_PROGRESS = "user_quests_marketplace_job_progress";
  public static final String TABLE_EQUIPMENT = "equipment";
  public static final String TABLE_TASKS = "tasks";
  public static final String TABLE_TASKS_EQUIPREQS = "tasks_equipreqs";
  public static final String TABLE_CITIES = "cities";
  public static final String TABLE_IAP_HISTORY = "iap_history";
  public static final String TABLE_MARKETPLACE = "marketplace";
  public static final String TABLE_MARKETPLACE_TRANSACTION_HISTORY = "marketplace_transaction_history";
  public static final String TABLE_JOBS_BUILD_STRUCT = "jobs_build_struct";
  public static final String TABLE_JOBS_UPGRADE_STRUCT = "jobs_upgrade_struct";
  public static final String TABLE_JOBS_DEFEAT_TYPE = "jobs_defeat_type";
  public static final String TABLE_JOBS_MARKETPLACE = "jobs_marketplace";
  public static final String TABLE_JOBS_POSSESS_EQUIP = "jobs_possess_equip";
  public static final String TABLE_QUESTS = "quests";
  public static final String TABLE_STRUCTURES = "structures";  
  
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
  public static final String USER__SKILL_POINTS = "skill_points";
  public static final String USER__HEALTH_MAX = "health_max";
  public static final String USER__ENERGY_MAX = "energy_max";
  public static final String USER__STAMINA_MAX = "stamina_max";
  public static final String USER__DIAMONDS = "diamonds";
  public static final String USER__COINS = "coins";
  public static final String USER__WOOD = "wood";
  public static final String USER__MARKETPLACE_DIAMONDS_EARNINGS = "marketplace_diamond_earnings";
  public static final String USER__MARKETPLACE_COINS_EARNINGS = "marketplace_coins_earnings";
  public static final String USER__MARKETPLACE_WOOD_EARNINGS = "marketplace_wood_earnings";
  public static final String USER__VAULT_BALANCE = "vault_balance";
  public static final String USER__EXPERIENCE = "experience";
  public static final String USER__TASKS_COMPLETED = "tasks_completed";
  public static final String USER__BATTLES_WON = "battles_won";
  public static final String USER__BATTLES_LOST = "battles_lost";
  
  public static final String USER__UDID = "udid";
  public static final String USER__LATITUDE = "latitude";
  public static final String USER__LONGITUDE = "longitude";
  public static final String USER__NUM_POSTS_IN_MARKETPLACE = "num_posts_in_marketplace";

  
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
  public static final String MARKETPLACE__POSTED_WOOD = "posted_wood";
  public static final String MARKETPLACE__POSTED_DIAMONDS = "posted_diamonds"; 
  public static final String MARKETPLACE__POSTED_COINS = "posted_coins";
  public static final String MARKETPLACE__DIAMOND_COST = "diamond_cost";
  public static final String MARKETPLACE__COIN_COST = "coin_cost";
  public static final String MARKETPLACE__WOOD_COST = "wood_cost";
  
  /*MARKETPLACE HISTORY TABLE*/
  public static final String MARKETPLACE_TRANSACTION_HISTORY__MARKETPLACE_ID = "marketplace_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID = "poster_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__BUYER_ID = "buyer_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POST_TYPE = "post_type";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_POST = "time_of_post";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_EQUIP_ID = "posted_equip_id";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_WOOD = "posted_wood";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_DIAMONDS = "posted_diamonds"; 
  public static final String MARKETPLACE_TRANSACTION_HISTORY__POSTED_COINS = "posted_coins";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__DIAMOND_COST = "diamond_cost";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__COIN_COST = "coin_cost";
  public static final String MARKETPLACE_TRANSACTION_HISTORY__WOOD_COST = "wood_cost";
  
  /*USER STRUCTS TABLE*/
  public static final String USER_STRUCTS__ID = GENERIC__ID;
  public static final String USER_STRUCTS__USER_ID = GENERIC__USER_ID;
  public static final String USER_STRUCTS__STRUCT_ID = "struct_id";
  public static final String USER_STRUCTS__LAST_RETRIEVED = "last_retrieved";
  public static final String USER_STRUCTS__X_COORD = "xcoord";
  public static final String USER_STRUCTS__Y_COORD = "ycoord";
  public static final String USER_STRUCTS__IS_COMPLETE = "is_complete";
  public static final String USER_STRUCTS__LEVEL = "level";
  
  /*USER CRITSTRUCTS TABLE*/
  public static final String USER_CRITSTRUCTS__USER_ID = GENERIC__USER_ID;
  public static final String USER_CRITSTRUCTS__ARMORY_X_COORD = "armory_xcoord";
  public static final String USER_CRITSTRUCTS__ARMORY_Y_COORD = "armory_ycoord";
  public static final String USER_CRITSTRUCTS__VAULT_X_COORD = "vault_xcoord";
  public static final String USER_CRITSTRUCTS__VAULT_Y_COORD = "vault_ycoord";
  public static final String USER_CRITSTRUCTS__MARKETPLACE_X_COORD = "marketplace_xcoord";
  public static final String USER_CRITSTRUCTS__MARKETPLACE_Y_COORD = "marketplace_ycoord";
  public static final String USER_CRITSTRUCTS__LUMBERMILL_X_COORD = "lumbermill_xcoord";
  public static final String USER_CRITSTRUCTS__LUMBERMILL_Y_COORD = "lumbermill_ycoord";
  public static final String USER_CRITSTRUCTS__CARPENTER_X_COORD = "carpenter_xcoord";
  public static final String USER_CRITSTRUCTS__CARPENTER_Y_COORD = "carpenter_ycoord";
  public static final String USER_CRITSTRUCTS__AVIARY_X_COORD = "aviary_xcoord";
  public static final String USER_CRITSTRUCTS__AVIARY_Y_COORD = "aviary_ycoord";
  
  /*USER QUESTS TABLE*/
  public static final String USER_QUESTS__USER_ID = GENERIC__USER_ID;
  public static final String USER_QUESTS__QUEST_ID = "quest_id";
  public static final String USER_QUESTS__IS_REDEEMED = "is_redeemed"; 
  public static final String USER_QUESTS__TASKS_COMPLETE = "tasks_complete"; 
  public static final String USER_QUESTS__DEFEAT_TYPE_JOBS_COMPLETE = "defeat_type_jobs_complete"; 
  public static final String USER_QUESTS__MARKETPLACE_JOBS_COMPLETE = "marketplace_type_jobs_complete"; 

  /*USER QUESTS COMPLETED TASKS TABLE*/
  public static final String USER_QUESTS_COMPLETED_TASKS__USER_ID = GENERIC__USER_ID;
  public static final String USER_QUESTS_COMPLETED_TASKS__QUEST_ID = "quest_id";
  public static final String USER_QUESTS_COMPLETED_TASKS__COMPLETED_TASK_ID = "completed_task_id";

  /*USER QUESTS COMPLETED DEFEAT TYPE JOBS TABLE*/
  public static final String USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__USER_ID = GENERIC__USER_ID;
  public static final String USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__QUEST_ID = "quest_id";
  public static final String USER_QUESTS_COMPLETED_DEFEAT_TYPE_JOBS__COMPLETED_DEFEAT_TYPE_JOB_ID = "completed_defeat_type_job_id";

  /*USER DEFEAT TYPE JOB PROGRESS*/
  public static final String USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__USER_ID = GENERIC__USER_ID;
  public static final String USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__QUEST_ID = "quest_id";
  public static final String USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__DEFEAT_TYPE_JOB_ID = "defeat_type_job_id";
  public static final String USER_QUESTS_DEFEAT_TYPE_JOB_PROGRESS__NUM_DEFEATED = "num_defeated";

  
}
