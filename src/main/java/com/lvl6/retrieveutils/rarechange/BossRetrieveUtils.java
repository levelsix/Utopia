package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Boss;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BossRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<Boss>> cityIdsToBosses;
  private static Map<Integer, Boss> bossIdsToBosses;

  private static final String TABLE_NAME = DBConstants.TABLE_BOSSES;

  public static Map<Integer, Boss> getBossIdsToBosses() {
    log.debug("retrieving all bosses data map");
    if (bossIdsToBosses == null) {
      setStaticBossIdsToBosses();
    }
    return bossIdsToBosses;
  }

  public static Boss getBossForBossId(int bossId) {
    log.debug("retrieve boss data for boss " + bossId);
    if (bossIdsToBosses == null) {
      setStaticBossIdsToBosses();      
    }
    return bossIdsToBosses.get(bossId);
  }

  public static Map<Integer, Boss> getBossesForBossIds(List<Integer> ids) {
    log.debug("retrieve boss data for bossids " + ids);
    if (bossIdsToBosses == null) {
      setStaticBossIdsToBosses();      
    }
    Map<Integer, Boss> toreturn = new HashMap<Integer, Boss>();
    for (Integer id : ids) {
      toreturn.put(id,  bossIdsToBosses.get(id));
    }
    return toreturn;
  }

  public static List<Boss> getAllBossesForCityId(int cityId) {
    log.debug("retrieving all bosses for cityId " + cityId);
    if (cityIdsToBosses == null) {
      setStaticCityIdsToBosses();
    }
    return cityIdsToBosses.get(cityId);
  }

  private static void setStaticCityIdsToBosses() {
    log.debug("setting static map of cityId to bosses");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map<Integer, List<Boss>> cityIdToBossesTemp = new HashMap<Integer, List<Boss>>();
          while(rs.next()) {  //should only be one
            Boss boss = convertRSRowToBoss(rs);
            if (boss != null) {
              if (cityIdToBossesTemp.get(boss.getCityId()) == null) {
                cityIdToBossesTemp.put(boss.getCityId(), new ArrayList<Boss>());
              }
              cityIdToBossesTemp.get(boss.getCityId()).add(boss);
            }
          }
          cityIdsToBosses = cityIdToBossesTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  private static void setStaticBossIdsToBosses() {
    log.debug("setting static map of bossIds to bosses");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, Boss> bossIdsToBossesTemp = new HashMap<Integer, Boss>();
          while(rs.next()) {  //should only be one
            Boss boss = convertRSRowToBoss(rs);
            if (boss != null)
              bossIdsToBossesTemp.put(boss.getId(), boss);
          }
          bossIdsToBosses = bossIdsToBossesTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticCityIdsToBosses();
    setStaticBossIdsToBosses();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Boss convertRSRowToBoss(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int cityId = rs.getInt(i++);
    int assetNumWithinCity = rs.getInt(i++);
    int baseHealth = rs.getInt(i++);
    int staminaCost = rs.getInt(i++);
    int minDamage = rs.getInt(i++);
    int maxDamage = rs.getInt(i++);
    int minutesToKill = rs.getInt(i++);
    int minutesToRespawn = rs.getInt(i++);
    int experienceGained = rs.getInt(i++);
    
    Boss boss = new Boss(id, cityId, assetNumWithinCity, staminaCost, minDamage, maxDamage, minutesToKill, minutesToRespawn, baseHealth, experienceGained);
    return boss;
  }
}