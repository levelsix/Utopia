package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.CityGem;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class CityGemRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, CityGem> activeCityGemIdsToCityGems;

  private static final String TABLE_NAME = DBConstants.TABLE_CITY_GEMS;
  
  public static List<Integer> getNonbossGemsIds() {
    List<Integer> returnValue = new ArrayList<Integer>();
    if (activeCityGemIdsToCityGems == null) {
      setStaticActiveCityGemIdsToCityGems();
    }
    if (null == activeCityGemIdsToCityGems) {
      log.error("unexpected error: no city gems exist");
      return returnValue;
    }
    
    for (CityGem cg : activeCityGemIdsToCityGems.values()) {
      if (!cg.isDroppedOnlyFromBosses()) {
        int id = cg.getId();
        returnValue.add(id);
      }
    }
    
    return returnValue;
  }

  public static CityGem getCityGemForId(int gemId) {
    if (activeCityGemIdsToCityGems == null) {
      setStaticActiveCityGemIdsToCityGems();
    }
    return activeCityGemIdsToCityGems.get(gemId);
  }
  
  public static Map<Integer, CityGem> getActiveCityGemIdsToCityGems() {
    log.debug("retrieving all cityGems data map");
    if (activeCityGemIdsToCityGems == null) {
      setStaticActiveCityGemIdsToCityGems();
    }
    return activeCityGemIdsToCityGems;
  }

  private static void setStaticActiveCityGemIdsToCityGems() {
    log.debug("setting static map of active cityGemIds to cityGems");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      //TODO:
      //should not get the whole table, only the ones that are active
      //rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
      absoluteConditionParams.put(DBConstants.CITY_GEMS__IS_ACTIVE, true);
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, CityGem> activeCityGemIdsToCityGemsTemp = new HashMap<Integer, CityGem>();
          while(rs.next()) {  //should only be one
            CityGem cityGem = convertRSRowToCityGem(rs);
            if (cityGem != null)
              activeCityGemIdsToCityGemsTemp.put(cityGem.getId(), cityGem);
          }
          activeCityGemIdsToCityGems = activeCityGemIdsToCityGemsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticActiveCityGemIdsToCityGems();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static CityGem convertRSRowToCityGem(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    float dropRate = rs.getFloat(i++);
    boolean isActive = rs.getBoolean(i++);
    String gemImageName = rs.getString(i++);
    boolean droppedOnlyFromBosses = rs.getBoolean(i++);

    CityGem cityGem = new CityGem(id, dropRate, isActive, gemImageName,
        droppedOnlyFromBosses);
    return cityGem;
  }
}
