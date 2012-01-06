package com.lvl6.properties;

public class DBConstants {

  /* TABLENAMES*/
  public static final String TABLE_USER = "users";
  public static final String TABLE_USER_EQUIP = "user_equip";
  public static final String TABLE_EQUIPMENT = "equipment";
  public static final String TABLE_TASKS = "tasks";
  public static final String TABLE_TASKS_EQUIPREQS = "tasks_equipreqs";

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


}
