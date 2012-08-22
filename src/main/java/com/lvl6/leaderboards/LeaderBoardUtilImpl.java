package com.lvl6.leaderboards;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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

//  @Override
//  public void incrementBattlesWonForUser(Integer userId) {
//    jedis.zincrby(LeaderBoardConstants.BATTLES_WON, 1d, userId.toString());
//  }
//  @Override
//  public void incrementBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
//    jedis.zincrby(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, 1d, userId.toString());		
//  }
  
  @Override
  public void setBattlesWonForUser(Integer userId, Double battlesWon) {
    jedis.zadd(LeaderBoardConstants.BATTLES_WON, battlesWon, userId.toString());
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
  public double getBattlesWonOverTotalBattlesRatioForUser(Integer userId) {
    return jedis.zscore(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, userId.toString());
  }
  
  @Override
  public void setTotalCoinValueForUser(Integer userId, Double coinWorth) {
    jedis.zadd(LeaderBoardConstants.COIN_WORTH, coinWorth, userId.toString());
  }
  @Override
  public void setExperienceForUser(Integer userId, Double experience) {
    jedis.zadd(LeaderBoardConstants.EXPERIENCE, experience, userId.toString());
  }
  
//  @Override
//  public void incrementTotalCoinValueForUser(Integer userId, Double amount) {
//    jedis.zincrby(LeaderBoardConstants.SILVER, amount, userId.toString());
//  }
  @Override
  public double getTotalCoinValueForUser(Integer userId) {
    return jedis.zscore(LeaderBoardConstants.COIN_WORTH, userId.toString());
  }
  @Override
  public double getExperienceForUser(Integer userId) {
    return jedis.zscore(LeaderBoardConstants.EXPERIENCE, userId.toString());
  }
  
  @Override
  public double getBattlesWonRankForUser(Integer userId) {
    return jedis.zrevrank(LeaderBoardConstants.BATTLES_WON, userId.toString());
  }
  @Override
  public double getBattlesWonOverTotalBattlesRatioRankForUser(Integer userId) {
    return jedis.zrevrank(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, userId.toString());
  }
  @Override
  public double getTotalCoinValueRankForUser(Integer userId) {
    return jedis.zrevrank(LeaderBoardConstants.COIN_WORTH, userId.toString());
  }
  @Override
  public double getExperienceRankForUser(Integer userId) {
    return jedis.zrevrank(LeaderBoardConstants.EXPERIENCE, userId.toString());
  }
  
  
  @Override
  public List<Integer> getBattlesWonTopN(Integer start, Integer stop) {
    Set<String> ids = jedis.zrevrange(LeaderBoardConstants.BATTLES_WON, start, stop);
    return convertToIdStringsToInts(ids);
  }
  @Override
  public List<Integer> getBattlesWonOverTotalBattlesRatioTopN(Integer start,Integer stop) {
    Set<String> ids = jedis.zrevrange(LeaderBoardConstants.BATTLES_WON_TO_TOTAL_BATTLES_RATIO, start, stop);
    return convertToIdStringsToInts(ids);
  }
  @Override
  public List<Integer> getTotalCoinValueForTopN(Integer start, Integer stop) {
    Set<String> ids = jedis.zrevrange(LeaderBoardConstants.COIN_WORTH, start, stop);
    return convertToIdStringsToInts(ids);
  }
  @Override
  public List<Integer> getExperienceTopN(Integer start, Integer stop) {
    Set<String> ids = jedis.zrevrange(LeaderBoardConstants.EXPERIENCE, start, stop);
    return convertToIdStringsToInts(ids);
  }

  protected List<Integer> convertToIdStringsToInts(Set<String> ids) {
    List<Integer> userIds = new ArrayList<Integer>();
    if(ids != null) {
      for(String id:ids) {
        userIds.add(Integer.parseInt(id));
      }
    }
    return userIds;
  }

//  @Override
//  public void updateLeaderboardForUser(Integer userId) {
//    try {
//      List<UserLeaderBoardStats> stats = jdbc.query("select " + DBConstants.USER__COINS + ", " + DBConstants.USER__VAULT_BALANCE + ", " + DBConstants.USER__BATTLES_WON + ", " + DBConstants.USER__EXPERIENCE + ", " + 
//          DBConstants.USER__BATTLES_LOST + " from " + DBConstants.TABLE_USER + " where " + DBConstants.USER__ID + " = " + userId,
//          new RowMapper<UserLeaderBoardStats>() {
//        @Override
//        public UserLeaderBoardStats mapRow(ResultSet rs, int rowNum)
//            throws SQLException {
//          UserLeaderBoardStats stats = new UserLeaderBoardStats();
//          stats.setBattles_lost(rs.getInt(DBConstants.USER__BATTLES_LOST));
//          stats.setBattles_won(rs.getInt(DBConstants.USER__BATTLES_WON));
//          stats.setTotalCoinValue(rs.getInt(DBConstants.USER__COINS));
//          stats.setVault_balance(rs.getInt(DBConstants.USER__VAULT_BALANCE));
//          stats.setExperience(rs.getInt(DBConstants.USER__EXPERIENCE));
//          return stats;
//        }
//
//      });
//      UserLeaderBoardStats stat = stats.get(0);
//
//      if(stat != null) {
//        setBattlesWonForUser(userId, stat.getBattles_won().doubleValue());
//        if(stat.battles_lost+stat.battles_won > ControllerConstants.LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION) {
//          setBattlesWonOverTotalBattlesRatioForUser(userId, stat.battlesWonOfTotalBattles());
//        } else {
//          setBattlesWonOverTotalBattlesRatioForUser(userId, 0.0);				  
//        }
//        setTotalCoinValueForUser(userId, (double) stat.getTotalCoinValue());
//      }
//
//    }catch(Exception e) {
//      log.error("Error updating leaderboard for user: "+userId, e);
//    }
//  }


  @Override
  public void updateLeaderboardForUser(User user) {
    if (user != null) {
      try {
        setBattlesWonForUser(user.getId(), (double) user.getBattlesWon());

        if (user.getBattlesWon() + user.getBattlesLost() > ControllerConstants.LEADERBOARD__MIN_BATTLES_REQUIRED_FOR_KDR_CONSIDERATION) {
          setBattlesWonOverTotalBattlesRatioForUser(user.getId(), (double) (user.getBattlesWon() / (user.getBattlesLost() + user.getBattlesWon())));
        } else {
          setBattlesWonOverTotalBattlesRatioForUser(user.getId(), 0.0);         
        }
        setTotalCoinValueForUser(user.getId(), (double) user.getCoins() + user.getVaultBalance());
        setExperienceForUser(user.getId(), (double) user.getExperience());
        
      } catch(Exception e) {
        log.error("Error updating leaderboard for user: " + user, e);
      }
    }
  }
  



  //	 @Override
  //	  public void updateLeaderboardTotalCoinValueForUser(Integer userId) {
  //	    try {
  //	      Long coins = jdbc.queryForLong("select " + DBConstants.USER__COINS + " from user where id = ?", userId);
  //	      if(coins != null) {
  //	        setTotalCoinValueForUser(userId, coins.doubleValue());
  //	      }
  //	    }catch(Exception e) {
  //	      log.error("Error updating leaderboard coins for user: "+userId, e);
  //	    }
  //	  }
//
//  protected class UserLeaderBoardStats{
//    int coins;
//    int vault_balance;
//    Integer battles_won;
//    Integer battles_lost;
//    Integer experience;
//
//    public int getTotalCoinValue() {
//      return coins;
//    }
//    public void setTotalCoinValue(int coins) {
//      this.coins = coins;
//    }
//
//    public Integer getBattles_won() {
//      return battles_won;
//    }
//    public void setBattles_won(Integer battles_won) {
//      this.battles_won = battles_won;
//    }
//
//    public Integer getBattles_lost() {
//      return battles_lost;
//    }
//    public void setBattles_lost(Integer battles_lost) {
//      this.battles_lost = battles_lost;
//    }
//
//    public int getVault_balance() {
//      return vault_balance;
//    }
//    public void setVault_balance(int vault_balance) {
//      this.vault_balance = vault_balance;
//    }
//
//    public int getTotalCoinWorth() {
//      return this.coins + this.vault_balance;
//    }
//
//    public Double battlesWonOfTotalBattles() {
//      return battles_won.doubleValue()/(battles_lost.doubleValue()+battles_won);
//    }
//
//    public Integer getExperience() {
//      return experience;
//    }
//    public void setExperience(Integer experience) {
//      this.experience = experience;
//    }
//  }


}
