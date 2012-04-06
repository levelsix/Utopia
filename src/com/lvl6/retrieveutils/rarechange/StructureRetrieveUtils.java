package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.Structure;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class StructureRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Structure> structIdsToStructs;

  private static final String TABLE_NAME = DBConstants.TABLE_STRUCTURES;

  public static Map<Integer, Structure> getStructIdsToStructs() {
    log.info("retrieving all-structs data");
    if (structIdsToStructs == null) {
      setStaticStructIdsToStructs();
    }
    return structIdsToStructs;
  }

  public static Structure getStructForStructId(int structId) {
    log.info("retrieve struct data");
    if (structIdsToStructs == null) {
      setStaticStructIdsToStructs();      
    }
    return structIdsToStructs.get(structId);
  }

  private static void setStaticStructIdsToStructs() {
    log.info("setting static map of structIds to structs");

    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectWholeTable(conn, TABLE_NAME);

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
    DBConnection.close(rs, null, conn);
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
    int instaRetrieveDiamondCost = rs.getInt(i++);
    int instaUpgradeDiamondCost = rs.getInt(i++);
    int xOffset = rs.getInt(i++);
    
    if (coinPriceSet && diamondPriceSet) {
      log.error("struct cannot have coin price and diamond price");
      return null;
    }

    return new Structure(id, name, income, minutesToGain, minutesToBuild, minutesToUpgradeBase, coinPrice, 
        diamondPrice, minLevel, xLength, yLength, instaBuildDiamondCost, instaRetrieveDiamondCost, 
        instaUpgradeDiamondCost, xOffset);
  }
}
