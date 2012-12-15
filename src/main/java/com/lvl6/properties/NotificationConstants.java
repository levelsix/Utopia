package com.lvl6.properties;

//TODO: FIGURE OUT WHAT VALUES TO SET THE COLORS
public class NotificationConstants {

  //COLOR CONSTANTS
  //CLAN TOWER WAR 	
  public static final int CLAN_CONCEDED__BLUE = 0;
  public static final int CLAN_CONCEDED__GREEN = 220;
  public static final int CLAN_CONCEDED__RED = 255;

  public static final int CLAN_WON__BLUE = 0;
  public static final int CLAN_WON__GREEN = 220;
  public static final int CLAN_WON__RED = 255;

  public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__BLUE = 0;
  public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__GREEN = 220;
  public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__RED = 255;

  public static final int CLAN_TOWER_DISTRIBUTE_REWARDS__BLUE = 0;
  public static final int CLAN_TOWER_DISTRIBUTE_REWARDS__GREEN = 220;
  public static final int CLAN_TOWER_DISTRIBUTE_REWARDS__RED = 255;

  //EPIC WEAPON DROP
  public static final int EPIC_WEAPON_DROPPED__BLUE = 0;
  public static final int EPIC_WEAPON_DROPPED__GREEN = 220;
  public static final int EPIC_WEAPON_DROPPED__RED = 255;

  //CLAN CREATED
  public static final int CLAN_CREATED__BLUE = 0;
  public static final int CLAN_CREATED__GREEN = 220;
  public static final int CLAN_CREATED__RED = 255;

  //CLAN TOWER TITLE AND SUBTITLE CONSTANTS (Formatted in accordance to MessageFormat class)
  public static String CLAN_CONCEDED__TITLE = "{0} has forfeited the {2} to {1}.";
  public static String CLAN_CONCEDED__SUBTITLE = "{1} now holds the {2} and wants a stronger opponent. ";
  public static String CLAN_CONCEDED__TITLE_NO_OWNER = "{0} has lost control of the {2}.";
  public static String CLAN_CONCEDED__SUBTITLE_NO_OWNER = "{2} is now free to be claimed!";

  public static String CLAN_WON__TITLE = "{0} has defeated {1} for possession of the {2}!";
  public static String CLAN_WON__SUBTITLE = "{0} now controls the {2} and is ready for another victim.";

  public static String CLAN_TOWER_ATTACKER_DETERMINED__TITLE = "{0} has declared war on {1} for the {2}!";
  public static String CLAN_TOWER_ATTACKER_DETERMINED__SUBTITLE = "The winner will be determined in 6 hours!";

  public static String CLAN_TOWER_OWNER_DETERMINED__TITLE = "{0} has claimed ownership of the {1}.";
  public static String CLAN_TOWER_OWNER_DETERMINED__SUBTITLE = "{0} is ready to wage war to maintain control.";

  public static String CLAN_TOWER_DISTRIBUTE_REWARDS__TITLE = "Your clan has held the {0} for {1} hours!";
  public static String CLAN_TOWER_DISTRIBUTE_REWARDS__SUBTITLE = "Everyone has received {2}! (It may take a moment)";


  //EPIC WEAPON DROP
  public static final String EPIC_WEAPON_DROPPED__TITLE = "{0} has found an epic item:";
  public static final String EPIC_WEAPON_DROPPED__SUBTITLE = "{1} in {2}"; //item_name in town_name

  //CLAN CREATED
  public static final String CLAN_CREATED__TITLE = "{0} has created a new clan named {1}!";
  public static final String CLAN_CREATED__SUBTITLE = "Go to the clan house to join {1}.";

}