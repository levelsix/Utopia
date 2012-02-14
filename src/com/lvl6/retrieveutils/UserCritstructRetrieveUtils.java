package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.UserCritstruct;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.utils.DBConnection;

public class UserCritstructRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_CITY_ELEMS;

  public static Map<CritStructType, UserCritstruct> getUserCritstructsForUser(int userId) {
    log.info("retrieving user critstructs for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CITY_ELEMS__USER_ID, userId);
    return convertRSToUserCritstructs(DBConnection.selectRowsByUserId(userId, TABLE_NAME));
  }
  
  private static Map<CritStructType, UserCritstruct> convertRSToUserCritstructs(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<CritStructType, UserCritstruct> critStructMap = new HashMap<CritStructType, UserCritstruct>();
        while(rs.next()) {
          addToCritStructMap(rs, critStructMap);
          return critStructMap;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static void addToCritStructMap(ResultSet rs, Map<CritStructType, UserCritstruct> critStructMap) throws SQLException {
    if (critStructMap == null) {
      return;
    }
    int i = 1;
    int armoryXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int armoryYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(armoryXCoord, armoryYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.ARMORY, new UserCritstruct(CritStructType.ARMORY, coords, orientation));
    }
    
    int vaultXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int vaultYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(vaultXCoord, vaultYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.VAULT, new UserCritstruct(CritStructType.VAULT, coords, orientation));
    }

    int marketplaceXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int marketplaceYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(marketplaceXCoord, marketplaceYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.MARKETPLACE, new UserCritstruct(CritStructType.MARKETPLACE, coords, orientation));
    }

    int lumbermillXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int lumbermillYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(lumbermillXCoord, lumbermillYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.LUMBERMILL, new UserCritstruct(CritStructType.LUMBERMILL, coords, orientation));
    }

    int carpenterXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int carpenterYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(carpenterXCoord, carpenterYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.CARPENTER, new UserCritstruct(CritStructType.CARPENTER, coords, orientation));
    }

    int aviaryXCoord = rs.getInt(i++);
    if (rs.wasNull()) {
      i++;
    } else {
      int aviaryYCoord = rs.getInt(i++);
      CoordinatePair coords = new CoordinatePair(aviaryXCoord, aviaryYCoord);
      StructOrientation orientation = StructOrientation.valueOf(rs.getInt(i++));
      critStructMap.put(CritStructType.AVIARY, new UserCritstruct(CritStructType.AVIARY, coords, orientation));
    }
  }

}
