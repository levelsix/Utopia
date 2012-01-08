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
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }    
    // TODO Auto-generated method stub
    
  }

  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static City convertRSRowToCity(ResultSet rs) throws SQLException {
    int id = rs.getInt(1);
    String name = rs.getString(2);
    int minLevel = rs.getInt(3);
    int expGainedBaseOnRankup = rs.getInt(4);
    int coinsGainedBaseOnRankup = rs.getInt(5);
    return new City(id, name, minLevel, expGainedBaseOnRankup, coinsGainedBaseOnRankup);
  }
}
