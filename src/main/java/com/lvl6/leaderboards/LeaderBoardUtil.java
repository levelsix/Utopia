package com.lvl6.leaderboards;

import java.util.Set;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import com.lvl6.info.User;

public interface LeaderBoardUtil {

	public abstract JedisPool getJedisPool();

	public abstract void setJedisPool(JedisPool jedis);

	public abstract void setBattlesWonForUser(Integer userId, Double battlesWon);
	public abstract void setBattlesWonForUser(Integer tournament, Integer userId, Double battlesWon);

	public abstract void setBattlesWonOverTotalBattlesRatioForUser(	Integer userId, Double battlesWonOfTotalBattles);

	public abstract void setTotalCoinValueForUser(Integer userId,
			Double coinValue);

	public abstract void setExperienceForUser(Integer userId, Double experience);

	//
	// public abstract void incrementBattlesWonForUser(Integer userId);
	// public abstract void
	// incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId);
	// public abstract void incrementTotalCoinValueForUser(Integer userId,
	// Double incrementAmount);
	//
	public abstract double getBattlesWonForUser(Integer userId);
	public abstract double getBattlesWonForUser(Integer tournament, Integer userId);
	
	
	public abstract double getBattlesWonOverTotalBattlesRatioForUser(
			Integer userId);

	public abstract double getTotalCoinValueForUser(Integer userId);

	public abstract double getExperienceForUser(Integer userId);

	public abstract long getBattlesWonRankForUser(Integer userId);

	public abstract long getBattlesWonOverTotalBattlesRatioRankForUser(
			Integer userId);

	public abstract long getTotalCoinValueRankForUser(Integer userId);

	public abstract long getExperienceRankForUser(Integer userId);

	public abstract Set<Tuple> getBattlesWonTopN(Integer start, Integer stop);

	public abstract Set<Tuple> getBattlesWonOverTotalBattlesRatioTopN(
			Integer start, Integer stop);

	public abstract Set<Tuple> getTotalCoinValueForTopN(Integer start,
			Integer stop);

	public abstract Set<Tuple> getExperienceTopN(Integer start, Integer stop);

	public abstract void updateLeaderboardForUser(User user);

	public abstract void setRankForEvent(Integer eventId, Integer userId, Double rank);
	public abstract double getRankForEvent(Integer eventId, Integer userId);
	public abstract Set<Tuple> getEventTopN(Integer eventId, Integer start, Integer stop);
	
	

}