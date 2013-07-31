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

import com.lvl6.info.City;
import com.lvl6.info.CoordinatePair;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class CityRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, City> cityIdToCity;

  private static final String TABLE_NAME = DBConstants.TABLE_CITIES;

  public static City getCityForCityId(int cityId) {
    log.debug("retrieving data for city with city id " + cityId);
    if (cityIdToCity == null) {
      setStaticCityIdsToCity();
    }
    return cityIdToCity.get(cityId);
  }

  public static Map<Integer, City> getCityIdsToCities() {
    log.debug("retrieving all cities data");
    if (cityIdToCity == null) {
      setStaticCityIdsToCity();
    }
    return cityIdToCity;
  }

  private static void setStaticCityIdsToCity() {
    log.debug("setting static map of cityIds to city");
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      try {
        rs.last();
        rs.beforeFirst();
        Map <Integer, City> cityIdToCityTemp = new HashMap<Integer, City>();
        while(rs.next()) {  //should only be one
          City city = convertRSRowToCity(rs);
          if (city != null)
            cityIdToCityTemp.put(city.getId(), city);
        }
        cityIdToCity = cityIdToCityTemp;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    DBConnection.get().close(rs, null, conn);
  }   
  // TODO Auto-generated method stub

public static void reload() {
  setStaticCityIdsToCity();
}


/*
 * assumes the resultset is apprpriately set up. traverses the row it's on.
 */
private static City convertRSRowToCity(ResultSet rs) throws SQLException {
  int i = 1;
  int id = rs.getInt(i++);
  String name = rs.getString(i++);
  int minLevel = rs.getInt(i++);
  int expGainedBaseOnRankup = rs.getInt(i++);
  int coinsGainedBaseOnRankup = rs.getInt(i++);
  String mapImgName = rs.getString(i++);
  CoordinatePair center = new CoordinatePair(rs.getFloat(i++), rs.getFloat(i++));
  int boosterPackId = rs.getInt(i++);
  
  return new City(id, name, minLevel, expGainedBaseOnRankup,
      coinsGainedBaseOnRankup, mapImgName, center, boosterPackId);
}

}
