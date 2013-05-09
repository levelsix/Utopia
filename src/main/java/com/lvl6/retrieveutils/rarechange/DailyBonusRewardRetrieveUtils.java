package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.DailyBonusReward;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class DailyBonusRewardRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, DailyBonusReward> dailyBonusRewardIdsToDailyBonusRewards;

  private static final String TABLE_NAME = DBConstants.TABLE_DAILY_BONUS_REWARD;

  public static Map<Integer, DailyBonusReward> getDailyBonusRewardIdsToDailyBonusRewards() {
    log.debug("retrieving all daily bonus rewards");
    if (dailyBonusRewardIdsToDailyBonusRewards == null) {
      setDailyBonusRewardIdsToDailyBonusRewards();
    }
    return dailyBonusRewardIdsToDailyBonusRewards;
  }

  private static void setDailyBonusRewardIdsToDailyBonusRewards() {
    log.debug("setting static map of daaily bonus reward ids to daily bonus rewards");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map<Integer, DailyBonusReward> temp = new HashMap<Integer, DailyBonusReward>();
          while(rs.next()) { 
            DailyBonusReward reward = convertRSRowToDailyBonusReward(rs);
            if (null != reward) {
              temp.put(reward.getId(), reward);
            }
          }
          dailyBonusRewardIdsToDailyBonusRewards = temp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setDailyBonusRewardIdsToDailyBonusRewards();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static DailyBonusReward convertRSRowToDailyBonusReward(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int minLevel = rs.getInt(i++);
    int maxLevel = rs.getInt(i++);
    int dayOneCoins = rs.getInt(i++);
    int dayTwoCoins = rs.getInt(i++);
    int dayThreeDiamonds = rs.getInt(i++);
    int dayFourCoins = rs.getInt(i++);
    
    String csvBoosterPackIds = rs.getString(i++);
    List<Integer> boosterPackIds = MiscMethods.unCsvStringIntoIntList(csvBoosterPackIds);
    
    return new DailyBonusReward(id, minLevel, maxLevel, dayOneCoins, dayTwoCoins, dayThreeDiamonds, dayFourCoins, boosterPackIds);
  }
}
