package com.lvl6.leaderboards;


import javax.annotation.Resource;

public class LeaderBoardUtilImpl implements LeaderBoardUtil {
	
	
	
	@Resource
	protected Lvl6Jedis jedis;
	/* (non-Javadoc)
	 * @see com.lvl6.leaderboards.LeaderBoardUtil#getJedis()
	 */
	@Override
	public Lvl6Jedis getJedis() {
		return jedis;
	}
	/* (non-Javadoc)
	 * @see com.lvl6.leaderboards.LeaderBoardUtil#setJedis(com.lvl6.leaderboards.Lvl6Jedis)
	 */
	@Override
	public void setJedis(Lvl6Jedis jedis) {
		this.jedis = jedis;
	}
	
	
	
	
	

	@Override
	public void incrementBattlesWonForUser(Integer userId) {
		jedis.zincrby(LeaderBoardConstants.BATTLES_WON, 1d, userId.toString());
		
	}
	@Override
	public void incrementTotalBattlesForUser(Integer userId) {
		jedis.zincrby(LeaderBoardConstants.BATTLES_TOTAL, 1d, userId.toString());
		
	}
	@Override
	public void incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
		jedis.zincrby(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, 1d, userId.toString());		
	}
	@Override
	public void setBattlesWonForUser(Integer userId, Double battlesWon) {
		jedis.zadd(LeaderBoardConstants.BATTLES_WON, battlesWon, userId.toString());
	}
	@Override
	public void setTotalBattlesForUser(Integer userId, Double totalBattles) {
		jedis.zadd(LeaderBoardConstants.BATTLES_TOTAL, totalBattles, userId.toString());		
	}
	@Override
	public void setBattlesWonOverTotalBattlesRatioForUser(Integer userId, Double battlesWonOfTotalBattles) {
		jedis.zadd(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, battlesWonOfTotalBattles, userId.toString());		
	}
	@Override
	public double getBattlesWonForUser(Integer userId) {
		return jedis.zscore(LeaderBoardConstants.BATTLES_WON, userId.toString());
	}
	@Override
	public double getTotalBattlesForUser(Integer userId) {
		return jedis.zscore(LeaderBoardConstants.BATTLES_TOTAL, userId.toString());
	}
	@Override
	public double getBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
		return jedis.zscore(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, userId.toString());
	}
	@Override
	public void setSilverForUser(Integer userId, Double silver) {
		jedis.zadd(LeaderBoardConstants.SILVER, silver, userId.toString());
	}
	@Override
	public void setTasksCompletedForUser(Integer userId, Double tasksCompleted) {
		jedis.zadd(LeaderBoardConstants.TASKS_COMPLETED, tasksCompleted, userId.toString());
	}
	@Override
	public void incrementSilverForUser(Integer userId, Double amount) {
		jedis.zincrby(LeaderBoardConstants.SILVER, amount, userId.toString());
		
	}
	@Override
	public void incrementTasksCompletedForUser(Integer userId) {
		jedis.zincrby(LeaderBoardConstants.TASKS_COMPLETED, 1d, userId.toString());
	}
	@Override
	public double getSilverForUser(Integer userId) {
		return jedis.zscore(LeaderBoardConstants.SILVER, userId.toString());
	}
	@Override
	public double getTasksCompletedForUser(Integer userId) {
		return jedis.zscore(LeaderBoardConstants.TASKS_COMPLETED, userId.toString());
	}
	
	
}
