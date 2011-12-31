package com.lvl6.properties;

public class DBConstants {

  /* TABLENAMES*/
  public static final String TABLE_USER = "users";
  public static final String TABLE_USER_EQUIP = "user_equip";
  public static final String TABLE_EQUIPMENT = "equipment";
  
  /*COLUMNNAMES*/
  public static final String GENERIC__USER_ID = "user_id";
  public static final String GENERIC__ID = "id";

  /*USER TABLE*/
  public static final String USER__ID = GENERIC__ID;
  
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
