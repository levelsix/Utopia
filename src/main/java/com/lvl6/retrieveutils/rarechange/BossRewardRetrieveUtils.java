package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BossReward;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BossRewardRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<BossReward>> bossIdsToBossRewards;
  private static Map<Integer, BossReward> idsToBossRewards;

  private static final String TABLE_NAME = DBConstants.TABLE_BOSS_REWARDS;

  public static Map<Integer, BossReward> getIdsToBossRewards() {
    log.debug("retrieving all boss rewards data map");
    if (idsToBossRewards == null) {
      setStaticIdsToBossRewards();
    }
    return idsToBossRewards;
  }

  public static BossReward getBossRewardForId(int id) {
    log.debug("retrieve boss reward data for id " + id);
    if (idsToBossRewards == null) {
      setStaticIdsToBossRewards();      
    }
    return idsToBossRewards.get(id);
  }

  public static Map<Integer, BossReward> getBossRewardsForIds(List<Integer> ids) {
    log.debug("retrieve boss rewards data for ids " + ids);
    if (idsToBossRewards == null) {
      setStaticIdsToBossRewards();      
    }
    Map<Integer, BossReward> toreturn = new HashMap<Integer, BossReward>();
    for (Integer id : ids) {
      toreturn.put(id,  idsToBossRewards.get(id));
    }
    return toreturn;
  }

  public static List<BossReward> getAllBossRewardsForBossId(int bossId) {
    log.debug("retrieving all bosses for bossId " + bossId);
    if (bossIdsToBossRewards == null) {
      setStaticBossIdsToBossRewards();
    }
    return bossIdsToBossRewards.get(bossId);
  }

  private static void setStaticBossIdsToBossRewards() {
    log.debug("setting static map of bossId to bossRewards");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map<Integer, List<BossReward>> bossIdsToBossRewardsTemp = new HashMap<Integer, List<BossReward>>();
          while(rs.next()) {  
            BossReward br = convertRSRowToBossReward(rs);
            if (br != null) {
              int bid = br.getBossId();
              if (bossIdsToBossRewardsTemp.get(bid) == null) {
                bossIdsToBossRewardsTemp.put(bid, new ArrayList<BossReward>());
              }
              bossIdsToBossRewardsTemp.get(bid).add(br);
            }
          }
          bossIdsToBossRewards = bossIdsToBossRewardsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  private static void setStaticIdsToBossRewards() {
    log.debug("setting static map of bossIds to bosses");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, BossReward> idsToBossRewardsTemp = new HashMap<Integer, BossReward>();
          while(rs.next()) {  //should only be one
            BossReward br = convertRSRowToBossReward(rs);
            if (br != null)
              idsToBossRewardsTemp.put(br.getId(), br);
          }
          idsToBossRewards = idsToBossRewardsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticIdsToBossRewards();
    setStaticBossIdsToBossRewards();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static BossReward convertRSRowToBossReward(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int bossId = rs.getInt(i++);
    int minSilver = rs.getInt(i++);
    if (minSilver == 0) minSilver = ControllerConstants.NOT_SET;
    int maxSilver = rs.getInt(i++);
    if (maxSilver == 0) maxSilver = ControllerConstants.NOT_SET;
    int minGold = rs.getInt(i++);
    if (minGold == 0) minGold = ControllerConstants.NOT_SET;
    int maxGold = rs.getInt(i++);
    if (maxGold == 0) maxGold = ControllerConstants.NOT_SET;
    int equipId = rs.getInt(i++);
    if (equipId == 0) equipId = ControllerConstants.NOT_SET;
    float probabilityToBeAwarded = rs.getFloat(i++);
    if (probabilityToBeAwarded == 0) probabilityToBeAwarded = ControllerConstants.NOT_SET;
    int rewardGroup = rs.getInt(i++);
    
    BossReward br = new BossReward(id, bossId, minSilver, maxSilver, minGold, maxGold, equipId, probabilityToBeAwarded, rewardGroup);
    return br;
  }
}
