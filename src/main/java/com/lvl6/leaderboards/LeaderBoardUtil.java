package com.lvl6.leaderboards;

import java.util.List;

public interface LeaderBoardUtil {

	public abstract Lvl6Jedis getJedis();

	public abstract void setJedis(Lvl6Jedis jedis);

	public abstract void setBattlesWonForUser(Integer userId, Double battlesWon);
	public abstract void setTotalBattlesForUser(Integer userId, Double totalBattles);
	public abstract void setBattlesWonOverTotalBattlesRatioForUser(Integer userId, Double battlesWonOfTotalBattles);
	public abstract void setSilverForUser(Integer userId, Double silver);
	public abstract void setTasksCompletedForUser(Integer userId, Double tasksCompleted);
	
	public abstract void incrementBattlesWonForUser(Integer userId);
	public abstract void incrementTotalBattlesForUser(Integer userId);
	public abstract void incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId);
	public abstract void incrementSilverForUser(Integer userId, Double incrementAmount);
	public abstract void incrementTasksCompletedForUser(Integer userId);

	public abstract double getBattlesWonForUser(Integer userId);
	public abstract double getTotalBattlesForUser(Integer userId);
	public abstract double getBattlesWonOverTotalBattlesRatioForUser(Integer userId);
	public abstract double getSilverForUser(Integer userId );
	public abstract double getTasksCompletedForUser(Integer userId);
	
	public abstract double getBattlesWonRankForUser(Integer userId);
	public abstract double getTotalBattlesRankForUser(Integer userId);
	public abstract double getBattlesWonOverTotalBattlesRatioRankForUser(Integer userId);
	public abstract double getSilverForRankUser(Integer userId );
	public abstract double getTasksCompletedRankForUser(Integer userId);

	
	public abstract List<Integer> getBattlesWonTopN(Integer start, Integer stop);
	public abstract List<Integer> getTotalBattlesTopN(Integer start, Integer stop);
	public abstract List<Integer> getBattlesWonOverTotalBattlesRatioTopN(Integer start, Integer stop);
	public abstract List<Integer> getSilverForTopN(Integer start, Integer stop );
	public abstract List<Integer> getTasksCompletedTopN(Integer start, Integer stop);
	
}