package com.lvl6.leaderboards;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

import com.lvl6.proto.InfoProto.LeaderboardType;

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
	/* (non-Javadoc)
	 * @see com.lvl6.leaderboards.LeaderBoardUtil#getJedis()
	 */
	@Override
	public JedisPool getJedisPool() {
		return jedisPool;
	}
	/* (non-Javadoc)
	 * @see com.lvl6.leaderboards.LeaderBoardUtil#setJedis(com.lvl6.leaderboards.Lvl6Jedis)
	 */
	@Override
	public void setJedisPool(JedisPool jedis) {
		this.jedisPool = jedis;
	}
	
	
	
	
	

	@Override
	public void incrementBattlesWonForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.zincrby(LeaderBoardConstants.BATTLES_WON, 1d, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void incrementTotalBattlesForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zincrby(LeaderBoardConstants.BATTLES_TOTAL, 1d, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zincrby(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, 1d, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void setBattlesWonForUser(Integer userId, Double battlesWon) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zadd(LeaderBoardConstants.BATTLES_WON, battlesWon, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void setTotalBattlesForUser(Integer userId, Double totalBattles) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zadd(LeaderBoardConstants.BATTLES_TOTAL, totalBattles, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void setBattlesWonOverTotalBattlesRatioForUser(Integer userId, Double battlesWonOfTotalBattles) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zadd(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, battlesWonOfTotalBattles, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public double getBattlesWonForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.zscore(LeaderBoardConstants.BATTLES_WON, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getTotalBattlesForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zscore(LeaderBoardConstants.BATTLES_TOTAL, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zscore(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public void setSilverForUser(Integer userId, Double silver) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zadd(LeaderBoardConstants.SILVER, silver, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void setTasksCompletedForUser(Integer userId, Double tasksCompleted) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zadd(LeaderBoardConstants.TASKS_COMPLETED, tasksCompleted, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void incrementSilverForUser(Integer userId, Double amount) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zincrby(LeaderBoardConstants.SILVER, amount, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public void incrementTasksCompletedForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		jedis.zincrby(LeaderBoardConstants.TASKS_COMPLETED, 1d, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
	}
	@Override
	public double getSilverForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zscore(LeaderBoardConstants.SILVER, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getTasksCompletedForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zscore(LeaderBoardConstants.TASKS_COMPLETED, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getBattlesWonRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zrevrank(LeaderBoardConstants.BATTLES_WON, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getTotalBattlesRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zrevrank(LeaderBoardConstants.BATTLES_TOTAL, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getBattlesWonOverTotalBattlesRatioRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zrevrank(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getSilverForRankUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zrevrank(LeaderBoardConstants.SILVER, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public double getTasksCompletedRankForUser(Integer userId) {
		Jedis jedis = jedisPool.getResource();
		try {
		return jedis.zrevrank(LeaderBoardConstants.TASKS_COMPLETED, userId.toString());
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return 0;
	}
	@Override
	public List<Integer> getBattlesWonTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
		Set<String> ids = jedis.zrevrange(LeaderBoardConstants.BATTLES_WON, start, stop);
		return convertToIdStringsToInts(ids);
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new ArrayList<Integer>();
	}
	
	@Override
	public List<Integer> getTotalBattlesTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
		Set<String> ids = jedis.zrevrange(LeaderBoardConstants.BATTLES_TOTAL, start, stop);
		return convertToIdStringsToInts(ids);
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new ArrayList<Integer>();
	}
	@Override
	public List<Integer> getBattlesWonOverTotalBattlesRatioTopN(Integer start,Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
		Set<String> ids = jedis.zrevrange(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, start, stop);
		return convertToIdStringsToInts(ids);
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new ArrayList<Integer>();
	}
	@Override
	public List<Integer> getSilverForTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
		Set<String> ids = jedis.zrevrange(LeaderBoardConstants.SILVER, start, stop);
		return convertToIdStringsToInts(ids);
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return null;
	}
	@Override
	public List<Integer> getTasksCompletedTopN(Integer start, Integer stop) {
		Jedis jedis = jedisPool.getResource();
		try {
		Set<String> ids = jedis.zrevrange(LeaderBoardConstants.TASKS_COMPLETED, start, stop);
		return convertToIdStringsToInts(ids);
		}catch(Exception e) {
			log.error("Error in jedis pool", e);
		}finally {
			if(jedis != null)
				jedisPool.returnResource(jedis);
		}
		return new ArrayList<Integer>();
	}


	protected List<Integer> convertToIdStringsToInts(Set<String> ids) {
		List<Integer> userIds = new ArrayList<Integer>();
		if(ids != null) {
			for(String id:ids) {
				userIds.add(Integer.getInteger(id));
			}
		}
		return userIds;
	}
	
	
	
	
	
	@Override
	public void updateLeaderboardForUser(Integer userId) {
		try {
			List<UserLeaderBoardStats> stats = jdbc.query("select coins, tasks_completed, battles_won, battles_lost from users where id = "+userId,
					new RowMapper<UserLeaderBoardStats>() {
						@Override
						public UserLeaderBoardStats mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							UserLeaderBoardStats stats = new UserLeaderBoardStats();
							stats.setBattles_lost(rs.getInt("battles_lost"));
							stats.setBattles_won(rs.getInt("battles_won"));
							stats.setCoins(rs.getLong("coins"));
							stats.setTasks_completed(rs.getInt("tasks_completed"));
							return stats;
						}
				
			});
			UserLeaderBoardStats stat = stats.get(0);
			if(stat != null) {
				if(stat.battles_lost+stat.battles_won > 50) {
					setBattlesWonForUser(userId, stat.getBattles_won().doubleValue());
					setBattlesWonOverTotalBattlesRatioForUser(userId, stat.battlesWonOfTotalBattles());
				}
				setTasksCompletedForUser(userId, stat.getTasks_completed().doubleValue());
				setSilverForUser(userId, stat.getCoins().doubleValue());
			}
		}catch(Exception e) {
			log.error("Error updating leaderboard for user: "+userId, e);
		}
	}
	
	@Override
	public void updateLeaderboardCoinsForUser(Integer userId) {
		try {
			Long silver = jdbc.queryForLong("select coins from user where id = ?", userId);
			if(silver != null) {
				setSilverForUser(userId, silver.doubleValue());
			}
		}catch(Exception e) {
			log.error("Error updating leaderboard coins for user: "+userId, e);
		}
	}

	
	
	protected class UserLeaderBoardStats{
		Long coins;
		Integer tasks_completed;
		Integer battles_won;
		Integer battles_lost;

		public Long getCoins() {
			return coins;
		}
		public void setCoins(Long coins) {
			this.coins = coins;
		}
		public Integer getTasks_completed() {
			return tasks_completed;
		}
		public void setTasks_completed(Integer tasks_completed) {
			this.tasks_completed = tasks_completed;
		}
		public Integer getBattles_won() {
			return battles_won;
		}
		public void setBattles_won(Integer battles_won) {
			this.battles_won = battles_won;
		}
		public Integer getBattles_lost() {
			return battles_lost;
		}
		public void setBattles_lost(Integer battles_lost) {
			this.battles_lost = battles_lost;
		}
		
		public Double battlesWonOfTotalBattles() {
			return battles_won.doubleValue()/(battles_lost.doubleValue()+battles_won);
		}
	}

	
}
