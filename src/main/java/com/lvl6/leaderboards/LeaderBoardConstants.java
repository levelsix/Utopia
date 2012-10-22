package com.lvl6.leaderboards;

public class LeaderBoardConstants {
  public static String BATTLES_WON = "battles_won";
  public static String BATTLES_TOTAL = "battles_total";
  public static String BATTLES_WON_TO_TOTAL_BATTLES_RATIO = "battles_won_to_battles_total_ratio";
  public static String COIN_WORTH = "coin_worth";
  public static String EXPERIENCE = "experience";
  
  public static String BATTLES_WON_FOR_TOURNAMENT(Integer tournament) {
	  return "battles_won_for_tournament_"+tournament;
  }

}