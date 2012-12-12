package com.lvl6.properties;

//TODO: FIGURE OUT WHAT VALUES TO SET THE COLORS
public class NotificationConstants {

	//CLAN TOWER WAR COLOR CONSTANTS	
	public static final int CLAN_CONCEDED__BLUE = 0;
	public static final int CLAN_CONCEDED__GREEN = 220;
	public static final int CLAN_CONCEDED__RED = 255;

	public static final int CLAN_WON__BLUE = 0;
	public static final int CLAN_WON__GREEN = 220;
	public static final int CLAN_WON__RED = 255;
	
	public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__BLUE = 0;
	public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__GREEN = 220;
	public static final int CLAN_TOWER_ATTACKER_OWNER_DETERMINED__RED = 255;
	
	//CLAN TOWER TITLE AND SUBTITLE CONSTANTS (Formatted in accordance to MessageFormat class)
	public static String CLAN_CONCEDED__TITLE = "{0} forfeited the {2} to {1}.";
	public static String CLAN_CONCEDED__SUBTITLE = "{1} now holds the {2} and wants a stronger opponent. ";

	public static String CLAN_WON__TITLE = "{0} defeated {1} for possession of the {2}!";
	public static String CLAN_WON__SUBTITLE = "{0} now controls the {2} and is ready for another victim.";
	
	public static String CLAN_TOWER_ATTACKER_DETERMINED__TITLE = "{0} declares war on {1} for the {2}!";
	
	public static String CLAN_TOWER_OWNER_DETERMINED__TITLE = "{0} has claimed ownership of the {1}.";
	public static String CLAN_TOWER_OWNER_DETERMINED__SUBTITLE = "{0} is ready to wage war to maintain control.";
	
	public static String CLAN_TOWER_DISTRIBUTE_REWARDS__TITLE = "Your clan won the war for the {0}!";
	public static String CLAN_TOWER_DISTRIBUTE_REWARDS__SUBTITLE = "Your leader has received";
	
	//TOOD: NEED NOTIFICATION FOR DISTRIBUTING REWARDS TO CLAN
	
}
