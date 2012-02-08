package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.City;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class CityRetrieveUtils {
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, City> cityIdToCity;
  
  private static final String TABLE_NAME = DBConstants.TABLE_CITIES;
    
  public static City getCityForCityId(int cityId) {
    log.info("retrieving city data");
    if (cityIdToCity == null) {
      setStaticCityIdsToCity();
    }
    return cityIdToCity.get(cityId);
  }
  
  public static Map<Integer, City> getCityIdsToCities() {
    log.info("retrieving all-cities data");
    if (cityIdToCity == null) {
      setStaticCityIdsToCity();
    }
    return cityIdToCity;
  }
  
  private static void setStaticCityIdsToCity() {
    log.info("setting static map of cityIds to city");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
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
        log.error("problem with database call.");
        log.error(e);
      }
    }    
    // TODO Auto-generated method stub
    
  }
  
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
    return new City(id, name, minLevel, expGainedBaseOnRankup, coinsGainedBaseOnRankup);
  }

}
