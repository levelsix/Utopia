package com.lvl6.leaderboards;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;

public class LeaderBoardUtilImpl implements LeaderBoardUtil {

	Logger log = LoggerFactory.getLogger(getClass());

	protected JdbcTemplate jdbc;

	@Resource
	protected DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbc = new JdbcTemplate(dataSource);
	}

	@Resource
	protected JedisPool jedisPool;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lvl6.leaderboards.LeaderBoardUtil#getJedis()
	 */
	@Override
	public JedisPool getJedisPool() {
		return jedisPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.lvl6.leaderboards.LeaderBoardUtil#setJedis(com.lvl6.leaderboards.
	 * Lvl6Jedis)
	 */
	@Override
	public void setJedisPool(JedisPool jedis) {
		this.jedisPool = jedis;
	}

	@Override
	public void setBattlesWonForUser(Integer userId, Double battlesWon) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.BATTLES_WON, battlesWon,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	@Override
	public void setBattlesWonOverTotalBattlesRatioForUser(Integer userId,
			Double battlesWonOfTotalBattles) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO,
					battlesWonOfTotalBattles, userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	@Override
	public double getBattlesWonForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.BATTLES_WON,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public double getBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO,userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public long getBattlesWonRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zrevrank(LeaderBoardConstants.BATTLES_WON,	userId.toString()) + 1;
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public long getBattlesWonOverTotalBattlesRatioRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zrevrank(
					LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO,
					userId.toString()) + 1;
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public Set<Tuple> getBattlesWonTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
			Set<Tuple> ids = jedis.zrevrangeWithScores(LeaderBoardConstants.BATTLES_WON,start, stop);
			return ids;//convertToIdStringsToInts(ids);
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new HashSet<Tuple>();
	}

	@Override
	public Set<Tuple> getBattlesWonOverTotalBattlesRatioTopN(Integer start,
			Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
			Set<Tuple> ids = jedis.zrevrangeWithScores(
					LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO,
					start, stop);
			return ids;//convertToIdStringsToInts(ids);
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new HashSet<Tuple>();
	}


	@Override
	public void setTotalCoinValueForUser(Integer userId, Double coinWorth) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.COIN_WORTH, coinWorth,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	@Override
	public void setExperienceForUser(Integer userId, Double experience) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.EXPERIENCE, experience,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	// @Override
	// public void incrementTotalCoinValueForUser(Integer userId, Double amount)
	// {
	// jedis.zincrby(LeaderBoardConstants.SILVER, amount, userId.toString());
	// }
	@Override
	public double getTotalCoinValueForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.COIN_WORTH,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public double getExperienceForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.EXPERIENCE,
					userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public long getTotalCoinValueRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zrevrank(LeaderBoardConstants.COIN_WORTH,
					userId.toString()) + 1;
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public long getExperienceRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zrevrank(LeaderBoardConstants.EXPERIENCE,
					userId.toString()) + 1;
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public Set<Tuple> getTotalCoinValueForTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
			Set<Tuple> ids = jedis.zrevrangeWithScores(LeaderBoardConstants.COIN_WORTH,
					start, stop);
			return ids;//convertToIdStringsToInts(ids);
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new HashSet<Tuple>();
	}

	@Override
	public Set<Tuple> getExperienceTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
			Set<Tuple> ids = jedis.zrevrangeWithScores(LeaderBoardConstants.EXPERIENCE,
					start, stop);
			return ids;//convertToIdStringsToInts(ids);
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new HashSet<Tuple>();
	}

	protected List<Integer> convertToIdStringsToInts(Set<String> ids) {
		List<Integer> userIds = new ArrayList<Integer>();
		if (ids != null) {
			for (String id : ids) {
				userIds.add(Integer.parseInt(id));
			}
		}
		return userIds;
	}

	@Override
	public void updateLeaderboardForUser(User user) {
		if (user != null) {
			long startTime = new Date().getTime();
			try {
				setBattlesWonForUser(user.getId(),
						(double) user.getBattlesWon());

				if (user.getBattlesWon() + user.getBattlesLost() > ControllerConstants.LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION) {
					setBattlesWonOverTotalBattlesRatioForUser(user.getId(),
						 ((double) user.getBattlesWon() / (user
									.getBattlesLost() + user.getBattlesWon())));
				} else {
					setBattlesWonOverTotalBattlesRatioForUser(user.getId(), 0.0);
				}
				setTotalCoinValueForUser(user.getId(), (double) user.getCoins()
						+ user.getVaultBalance());
				setExperienceForUser(user.getId(),
						(double) user.getExperience());

			} catch (Exception e) {
				log.error("Error updating leaderboard for user: " + user, e);
			}
			long endTime = new Date().getTime();
			log.info("Update Leaderboard for user {} took {}ms", user.getId(), endTime-startTime);
		}
	}

	@Override
	public void setBattlesWonForUser(Integer tournament, Integer userId,Double battlesWon) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.BATTLES_WON_FOR_TOURNAMENT(tournament), battlesWon,	userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	@Override
	public double getBattlesWonForUser(Integer tournament, Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.BATTLES_WON_FOR_TOURNAMENT(tournament), userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public void setScoreForEventAndUser(Integer eventId, Integer userId, Double score) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zadd(LeaderBoardConstants.RANK_FOR_EVENT(eventId), score, userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
	}

	@Override
	public double getScoreForEventAndUser(Integer eventId, Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.RANK_FOR_EVENT(eventId), userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	@Override
	public long getRankForEventAndUser(Integer eventId, Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zrank(LeaderBoardConstants.RANK_FOR_EVENT(eventId), userId.toString());
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}

	
	
	@Override
	public Set<Tuple> getEventTopN(Integer eventId, Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
			Set<Tuple> ids = jedis.zrangeWithScores(LeaderBoardConstants.RANK_FOR_EVENT(eventId),
					start, stop);
			return ids;//convertToIdStringsToInts(ids);
		} catch (Exception e) {
			log.error("Error in jedis pool", e);
		} finally {
			if (jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new HashSet<Tuple>();
	}

}
