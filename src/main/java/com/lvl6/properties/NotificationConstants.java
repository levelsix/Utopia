package com.lvl6.properties;

//TODO: FIGURE OUT WHAT VALUES TO SET THE COLORS
public class NotificationConstants {

  /*COLOR CONSTANTS*/
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

  //LEADERBOARD EVENT (TOURNAMENT) ENDED
  public static final int LEADERBOARD_EVENT_ENDED_GLOBAL__BLUE = 0;
  public static final int LEADERBOARD_EVENT_ENDED_GLOBAL__GREEN = 200;
  public static final int LEADERBOARD_EVENT_ENDED_GLOBAL__RED = 255;
  
  //TODO: FIX THESE NUMBERS!!!!!
  public static final int LEADERBOARD_EVENT_ENDED_INDIVIDUAL__BLUE = 0;
  public static final int LEADERBOARD_EVENT_ENDED_INDIVIDUAL__GREEN = 200;
  public static final int LEADERBOARD_EVENT_ENDED_INDIVIDUAL__RED = 255;

  //CLAN TOWER WAR BEGAN
  public static final int ATTACKING_CLAN_DETERMINED_APNS__BLUE = 0;
  public static final int ATTACKING_CLAN_DETERMINED_APNS__GREEN = 220;
  public static final int ATTACKING_CLAN_DETERMINED_APNS__RED = 255;
  
  //USER REQUESTED TO JOIN A CLAN
  public static final int REQUEST_TO_JOIN_A_CLAN__BLUE = 0;
  public static final int REQUEST_TO_JOIN_A_CLAN__GREEN = 220;
  public static final int REQUEST_TO_JOIN_A_CLAN__RED = 255;
  
  //USER JOINED A CLAN
  public static final int USER_JOINED_A_CLAN__BLUE = 0;
  public static final int USER_JOINED_A_CLAN__GREEN = 220;
  public static final int USER_JOINED_A_CLAN__RED = 255;

  //USER LEFT A CLAN
  public static final int USER_LEFT_A_CLAN__BLUE = 0;
  public static final int USER_LEFT_A_CLAN__GREEN = 220;
  public static final int USER_LEFT_A_CLAN__RED = 255;
  
  /*TITLE AND SUBTITLE CONSTANTS*/
  //THE ESCAPE CHARACTER IN MESSAGEFORMAT IS A SINGLE APOSTROPHE CHARACTER, '.
  //PRECEDE APOSTROPHES WITH ANOTHER APOSTROPHE TO DISPLAY ONE QUOTE TO THE USER. 
  
  //CLAN TOWER  (Formatted in accordance to MessageFormat class)
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

  //LEADERBOARD EVENT (TOURNAMENT) ENDED, MESSAGE FOR EVERYONE
  public static final String LEADERBOARD_EVENT_ENDED_GLOBAL_TITLE = "The tournament has just ended!";
  public static final String LEADERBOARD_EVENT_ENDED_GLOBAL_SUBTITLE = "{0} has won the grand prize.";
      
  //LEADERBOARD EVENT (TOURNAMENT) ENDED, MESSAGE FOR INDIVIDUALS THAT ARE ONLINE
  public static final String LEADERBOARD_EVENT_ENDED_INDIVIDUAL_ONLINE_TITLE = "You made it into the tournament''s top {0}!";
  public static final String LEADERBOARD_EVENT_ENDED_INDIVIDUAL_ONLINE_SUBTITLE = "You have been awarded {1} gold.";
  
  //LEADERBOARD EVENT (TOURNAMENT) ENDED, MESSAGE FOR INDIVIDUALS THAT ARE OFFLINE
  public static final String LEADERBOARD_EVENT_ENDED_INDIVIDUAL_OFFLINE_TITLE = "The tournament ended.";
  public static final String LEADERBOARD_EVENT_ENDED_INDIVIDUAL_OFFLINE_SUBTITLE = " You were in the top {0} and won {1} gold!";
  
  
  //FLAVOR TEXT FOR CLAN ATTACKING A TOWER
  public static String ATTACKING_CLAN__TITLE_APNS = "Your clan is waging war on {0} for the {1}!"; 
  public static final String[] ATTACKING_CLAN__SUBTITLE_APNS =  {
    "Fight! Come on, do you want to live forever?!",
    "By the power of Greyskull, YOU have the power! Attack!",
    "Tulta munille!",
    "CHARGE!",
    "Hit''em hard and hit''em fast!",
    "Attack them! Leeeroy Jennnkins!"
  };
  
  //FLAVOR TEXT FOR CLAN DEFENDING A TOWER
  public static String DEFENDING_CLAN__TITLE_APNS = "{0} is waging war against your clan for the {1}!";
  public static final String[] DEFENDING_CLAN__SUBTITLE_APNS = {
    "Strike back! This is where we fight! This is where they die!",
    "Red alert! Man your battle station!",
    "Eos oportet delenda est!",
    "Wipe them out!",
    "Counterattack! Today is a good day, for them, to die!",
    "Suit up! Attack!"
  };

  //USER REQUESTED TO JOIN A CLAN
  public static final String REQUEST_TO_JOIN_A_CLAN__TITLE = "Level {0} {1} wants to join your clan!";
  public static final String REQUEST_TO_JOIN_A_CLAN__SUBTITLE = "See if {1} is worthy.";

  //USER JOINED A CLAN
  public static final String USER_JOINED_A_CLAN__TITLE = "Level {0} {1} has joined the clan!";
  public static final String USER_JOINED_A_CLAN__SUBTITLE = "Welcome!";

  //USER JOINED A CLAN
  public static final String USER_LEFT_A_CLAN__TITLE = "Level {0} {1} has left the clan!";
  public static final String USER_LEFT_A_CLAN__SUBTITLE = " :(";

  //THE ESCAPE CHARACTER IN MESSAGEFORMAT IS A SINGLE APOSTROPHE CHARACTER, '.
  //PRECEDE APOSTROPHES WITH ANOTHER APOSTROPHE TO DISPLAY ONE QUOTE TO THE USER. 
}
