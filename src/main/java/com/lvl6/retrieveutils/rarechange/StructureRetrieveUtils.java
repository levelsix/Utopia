package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Structure;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class StructureRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Structure> structIdsToStructs;

  private static final String TABLE_NAME = DBConstants.TABLE_STRUCTURES;

  public static Map<Integer, Structure> getStructIdsToStructs() {
    log.debug("retrieving all structs data");
    if (structIdsToStructs == null) {
      setStaticStructIdsToStructs();
    }
    return structIdsToStructs;
  }

  public static Structure getStructForStructId(int structId) {
    log.debug("retrieve struct data for structId " + structId);
    if (structIdsToStructs == null) {
      setStaticStructIdsToStructs();      
    }
    return structIdsToStructs.get(structId);
  }

  private static void setStaticStructIdsToStructs() {
    log.debug("setting static map of structIds to structs");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, Structure> structIdsToStructsTemp = new HashMap<Integer, Structure>();
          while(rs.next()) {
            Structure struct = convertRSRowToStruct(rs);
            if (struct != null)
              structIdsToStructsTemp.put(struct.getId(), struct);
          }
          structIdsToStructs = structIdsToStructsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticStructIdsToStructs();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Structure convertRSRowToStruct(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    String name = rs.getString(i++);
    int income = rs.getInt(i++);
    int minutesToGain = rs.getInt(i++);
    int minutesToBuild = rs.getInt(i++);
    int minutesToUpgradeBase = rs.getInt(i++);
    int coinPrice = rs.getInt(i++);
    boolean coinPriceSet = !rs.wasNull();
    int diamondPrice = rs.getInt(i++);
    boolean diamondPriceSet = !rs.wasNull();
    int minLevel = rs.getInt(i++);
    int xLength = rs.getInt(i++);
    int yLength = rs.getInt(i++);
    int instaBuildDiamondCost = rs.getInt(i++);
    int instaRetrieveDiamondCostBase = rs.getInt(i++);
    int instaUpgradeDiamondCostBase = rs.getInt(i++);
    int imgVerticalPixelOffset = rs.getInt(i++);
    
    if (coinPriceSet && diamondPriceSet) {
      log.error("struct cannot have coin price and diamond price, and this structId violates it: " + id);
      return null;
    }

    return new Structure(id, name, income, minutesToGain, minutesToBuild, minutesToUpgradeBase, coinPrice, 
        diamondPrice, minLevel, xLength, yLength, instaBuildDiamondCost, instaRetrieveDiamondCostBase, 
        instaUpgradeDiamondCostBase, imgVerticalPixelOffset);
  }
}
