package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BoosterPack;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BoosterPackRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, BoosterPack> boosterPackIdsToBoosterPacks;

  private static final String TABLE_NAME = DBConstants.TABLE_BOOSTER_PACK;

  public static Map<Integer, BoosterPack> getBoosterPackIdsToBoosterPacks() {
    log.debug("retrieving all booster packs data map");
    if (boosterPackIdsToBoosterPacks == null) {
      setStaticBoosterPackIdsToBoosterPacks();
    }
    return boosterPackIdsToBoosterPacks;
  }

  public static BoosterPack getBoosterPackForBoosterPackId(int boosterPackId) {
    log.debug("retrieve booster pack data for booster pack " + boosterPackId);
    if (boosterPackIdsToBoosterPacks == null) {
      setStaticBoosterPackIdsToBoosterPacks();      
    }
    return boosterPackIdsToBoosterPacks.get(boosterPackId);
  }

  private static void setStaticBoosterPackIdsToBoosterPacks() {
    log.debug("setting static map of boosterPackIds to boosterPacks");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, BoosterPack> boosterPackIdsToBoosterPacksTemp = new HashMap<Integer, BoosterPack>();
          while(rs.next()) {  //should only be one
            BoosterPack boosterPack = convertRSRowToBoosterPack(rs);
            if (boosterPack != null)
              boosterPackIdsToBoosterPacksTemp.put(boosterPack.getId(), boosterPack);
          }
          boosterPackIdsToBoosterPacks = boosterPackIdsToBoosterPacksTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticBoosterPackIdsToBoosterPacks();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static BoosterPack convertRSRowToBoosterPack(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    boolean costsCoins = rs.getBoolean(i++);
    String name = rs.getString(i++);
    String chestImage = rs.getString(i++);
    String middleImage = rs.getString(i++);
    String backgroundImage = rs.getString(i++);
    int minLevel = rs.getInt(i++);
    int maxLevel = rs.getInt(i++);
    int dailyLimit = rs.getInt(i++);
    int salePriceOne = rs.getInt(i++);
    int retailPriceOne = rs.getInt(i++);
    int salePriceTwo = rs.getInt(i++);
    int retailPriceTwo = rs.getInt(i++);
    
    BoosterPack boosterPack = new BoosterPack(id, costsCoins, name, chestImage,
        middleImage, backgroundImage, minLevel, maxLevel, dailyLimit, salePriceOne, 
        retailPriceOne, salePriceTwo, retailPriceTwo);
    return boosterPack; 
  }
}
