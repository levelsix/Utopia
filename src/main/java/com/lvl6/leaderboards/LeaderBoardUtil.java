package com.lvl6.leaderboards;

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
	
}