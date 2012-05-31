package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.NeutralCityElementProto.NeutralCityElemType;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.utils.DBConnection;

public class NeutralCityElementsRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, List<NeutralCityElement>> cityIdToNeutralCityElements;

  private static final String TABLE_NAME = DBConstants.TABLE_NEUTRAL_CITY_ELEMENTS;

  public static Map<Integer, List<NeutralCityElement>> getCityIdToNeutralCityElements() {
    log.debug("retrieving all city id to neutral city elements data");
    if (cityIdToNeutralCityElements == null) {
      setStaticCityIdToNeutralCityElements();
    }
    return cityIdToNeutralCityElements;
  }

  public static NeutralCityElement getNeutralCityElement(int cityId, int assetId) {
    log.debug("retrieving neutral city element with assetId " + assetId + " in cityId " + cityId);
    if (cityIdToNeutralCityElements == null) {
      setStaticCityIdToNeutralCityElements();
    }
    List<NeutralCityElement> ncesForCity = cityIdToNeutralCityElements.get(cityId);
    if (ncesForCity != null) {
      for (NeutralCityElement nce : ncesForCity) {
        if (nce.getAssetId() == assetId) {
          return nce;
        }
      }
    }
    return null;
  }

  public static List<NeutralCityElement> getNeutralCityElementsForCity(int cityId) {
    log.debug("retrieving neutral city elements for city " + cityId);
    if (cityIdToNeutralCityElements == null) {
      setStaticCityIdToNeutralCityElements();
    }
    return cityIdToNeutralCityElements.get(cityId);
  }

  public static void reload() {
    setStaticCityIdToNeutralCityElements();
  }

  private static void setStaticCityIdToNeutralCityElements() {
    log.debug("setting static map of city id to neutral city elements for city");

    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, List<NeutralCityElement>> cityIdToNeutralCityElementsTemp = new HashMap<Integer, List<NeutralCityElement>>();
          while(rs.next()) {
            NeutralCityElement nce = convertRSRowToNeutralCityElement(rs);
            if (nce != null) {
              if (cityIdToNeutralCityElementsTemp.get(nce.getCityId()) == null) {
                cityIdToNeutralCityElementsTemp.put(nce.getCityId(), new ArrayList<NeutralCityElement>());
              }
              cityIdToNeutralCityElementsTemp.get(nce.getCityId()).add(nce);
            }
          }
          cityIdToNeutralCityElements = cityIdToNeutralCityElementsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }   
    }
    DBConnection.close(rs, null, conn);
  }

  private static NeutralCityElement convertRSRowToNeutralCityElement(ResultSet rs) throws SQLException {
    int i = 1;
    int cityId = rs.getInt(i++);
    int assetId = rs.getInt(i++);
    String goodName = rs.getString(i++);
    String badName = rs.getString(i++);
    NeutralCityElemType type = NeutralCityElemType.valueOf(rs.getInt(i++));
    CoordinatePair coords = new CoordinatePair(rs.getFloat(i++), rs.getFloat(i++));

    int xLength = rs.getInt(i++);
    if (rs.wasNull()) xLength = ControllerConstants.NOT_SET;

    int yLength = rs.getInt(i++);
    if (rs.wasNull()) yLength = ControllerConstants.NOT_SET;

    String imgIdGood = rs.getString(i++);
    String imgIdBad = rs.getString(i++);

    int orientationNum = rs.getInt(i++);
    StructOrientation orientation = (rs.wasNull()) ? null : StructOrientation.valueOf(orientationNum);

    return new NeutralCityElement(cityId, assetId, goodName, badName, type, coords, xLength, yLength, imgIdGood, imgIdBad, orientation);
  }


}