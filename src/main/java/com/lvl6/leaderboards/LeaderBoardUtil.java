package com.lvl6.leaderboards;

import java.util.List;

import com.lvl6.info.User;

public interface LeaderBoardUtil {

	public abstract Lvl6Jedis getJedis();

	public abstract void setJedis(Lvl6Jedis jedis);
	

	public abstract void setBattlesWonForUser(Integer userId, Double battlesWon);
	public abstract void setBattlesWonOverTotalBattlesRatioForUser(Integer userId, Double battlesWonOfTotalBattles);
	public abstract void setTotalCoinValueForUser(Integer userId, Double coinValue);
	public abstract void setExperienceForUser(Integer userId, Double experience);

//	
//	public abstract void incrementBattlesWonForUser(Integer userId);
//	public abstract void incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId);
//	public abstract void incrementTotalCoinValueForUser(Integer userId, Double incrementAmount);
//
	public abstract double getBattlesWonForUser(Integer userId);
	public abstract double getBattlesWonOverTotalBattlesRatioForUser(Integer userId);
	public abstract double getTotalCoinValueForUser(Integer userId);
  public abstract double getExperienceForUser(Integer userId);
	
	public abstract double getBattlesWonRankForUser(Integer userId);
	public abstract double getBattlesWonOverTotalBattlesRatioRankForUser(Integer userId);
	public abstract double getTotalCoinValueRankForUser(Integer userId);
  public abstract double getExperienceRankForUser(Integer userId);

	public abstract List<Integer> getBattlesWonTopN(Integer start, Integer stop);
	public abstract List<Integer> getBattlesWonOverTotalBattlesRatioTopN(Integer start, Integer stop);
	public abstract List<Integer> getTotalCoinValueForTopN(Integer start, Integer stop);
  public abstract List<Integer> getExperienceTopN(Integer start, Integer stop);

	
//	public abstract void updateLeaderboardForUser(Integer userId);
	public abstract void updateLeaderboardForUser(User user);

//	public abstract void updateLeaderboardTotalCoinValueForUser(Integer userId);
	
	
}