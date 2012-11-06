package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.ClanTierLevel;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanTierLevelRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, ClanTierLevel> allClanTierLevels;
  
  private static int highestClanTierLevel;

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_TIER_LEVELS;

  public static Map<Integer, ClanTierLevel> getAllClanTierLevels() {
    log.debug("retrieving all clan tier level data");
    if (allClanTierLevels == null) {
      setStaticAllClanTiers();
    }
    return allClanTierLevels;
  }

  public static ClanTierLevel getClanTierLevel(int tierLevel) {
    log.debug("retrieving clan tier level for tier level " + tierLevel);
    if (allClanTierLevels == null) {
      setStaticAllClanTiers();
    }
    return allClanTierLevels.get(tierLevel);
  }

  public static void reload() {
    setStaticAllClanTiers();
  }

  private static void setStaticAllClanTiers() {
    log.debug("setting static map of all clan tier levels");

    highestClanTierLevel = 1;
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, ClanTierLevel> allClanTiersTemp = new HashMap<Integer, ClanTierLevel>();
          while(rs.next()) {
            ClanTierLevel aClanTier = convertRSRowToClanTier(rs);
            if (null != aClanTier) {
            	allClanTiersTemp.put(aClanTier.getTierLevel(), aClanTier);
            }
          }
          allClanTierLevels = allClanTiersTemp;
          
          //clan tier levels start from 1 and if there are 5 rows,
          //then the max clan tier level is 5
          highestClanTierLevel = allClanTierLevels.size();
          
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }
    }
    DBConnection.get().close(rs, null, conn);
  }
  
  /*
   * assumes the resultset is appropriately set up. traverses the row it's on.
   */
  private static ClanTierLevel convertRSRowToClanTier(ResultSet rs) throws SQLException {
    int i = 1;
    int tierLevel = rs.getInt(i++);
    int clanSize = rs.getInt(i++);
    int tierUpgradeGoldCost = rs.getInt(i++);
    
    return new ClanTierLevel(tierLevel, clanSize, tierUpgradeGoldCost);
  }
 
  public static int getHighestClanTierLevel() {
	  return highestClanTierLevel;
  }
}


