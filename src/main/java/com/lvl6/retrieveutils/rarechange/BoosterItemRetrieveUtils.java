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

import com.lvl6.info.BoosterItem;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BoosterItemRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, BoosterItem> boosterItemIdsToBoosterItems;
  //key:booster pack id --> value:(key: booster item id --> value: booster item)
  private static Map<Integer, Map<Integer, BoosterItem>> 
      boosterItemIdsToBoosterItemsForBoosterPackIds;

  private static final String TABLE_NAME = DBConstants.TABLE_BOOSTER_ITEM;

  public static Map<Integer, BoosterItem> getBoosterItemIdsToBoosterItems() {
    log.debug("retrieving all BoosterItems data map");
    if (boosterItemIdsToBoosterItems == null) {
      setStaticBoosterItemIdsToBoosterItems();
    }
    return boosterItemIdsToBoosterItems;
  }
  
  public static Map<Integer, Map<Integer, BoosterItem>> getBoosterItemIdsToBoosterItemsForBoosterPackIds() {
    if(null == boosterItemIdsToBoosterItemsForBoosterPackIds) {
      setStaticBoosterItemIdsToBoosterItemsForBoosterPackIds();
    }
    return boosterItemIdsToBoosterItemsForBoosterPackIds;
  }

  public static Map<Integer, BoosterItem> getBoosterItemIdsToBoosterItemsForBoosterPackId(int boosterPackId) {
    try {
      log.debug("retrieve boosterPack data for boosterPack " + boosterPackId);
      if (boosterItemIdsToBoosterItems == null) {
        setStaticBoosterItemIdsToBoosterItems();
      }
      if (boosterItemIdsToBoosterItemsForBoosterPackIds == null) {
        boosterItemIdsToBoosterItemsForBoosterPackIds = new HashMap<Integer, Map<Integer, BoosterItem>>();
      }
      List<BoosterItem> bis = new ArrayList<BoosterItem>(boosterItemIdsToBoosterItems.values());
      for(BoosterItem bi : bis) {
        int packId = bi.getBoosterPackId();
        if(!boosterItemIdsToBoosterItemsForBoosterPackIds.containsKey(packId)) {
          Map<Integer, BoosterItem> bItemIdToBItem = new HashMap<Integer, BoosterItem>();
          boosterItemIdsToBoosterItemsForBoosterPackIds.put(packId, bItemIdToBItem);
        }
        //each itemId is unique (autoincrementing in the table)
        Map<Integer, BoosterItem> itemIdToItem =
            boosterItemIdsToBoosterItemsForBoosterPackIds.get(packId);
        itemIdToItem.put(bi.getId(), bi);
      }
      return boosterItemIdsToBoosterItemsForBoosterPackIds.get(boosterPackId);
    } catch (Exception e) {
      log.error("error creating a map of booster item ids to booster items.", e);
    }
    return null;
  }
  
  public static BoosterItem getBoosterItemForBoosterItemId(int boosterItemId) {
    log.debug("retrieve boosterItem data for boosterItem " + boosterItemId);
    if (boosterItemIdsToBoosterItems == null) {
      setStaticBoosterItemIdsToBoosterItems();      
    }
    return boosterItemIdsToBoosterItems.get(boosterItemId);
  }

  public static void setStaticBoosterItemIdsToBoosterItemsForBoosterPackIds() {
    try {
      log.debug("setting static map of boosterPackId to (boosterItemIds to boosterItems) ");
      if (boosterItemIdsToBoosterItemsForBoosterPackIds == null) {
        setStaticBoosterItemIdsToBoosterItems();      
        boosterItemIdsToBoosterItemsForBoosterPackIds = new HashMap<Integer, Map<Integer, BoosterItem>>();
      }
      List<BoosterItem> bis = new ArrayList<BoosterItem>(boosterItemIdsToBoosterItems.values());
      for(BoosterItem bi : bis) {
        int packId = bi.getBoosterPackId();
        if(!boosterItemIdsToBoosterItemsForBoosterPackIds.containsKey(packId)) {
          Map<Integer, BoosterItem> bItemIdToBItem = new HashMap<Integer, BoosterItem>();
          boosterItemIdsToBoosterItemsForBoosterPackIds.put(packId, bItemIdToBItem);
        }
        //each itemId is unique (autoincrementing in the table)
        Map<Integer, BoosterItem> itemIdToItem =
            boosterItemIdsToBoosterItemsForBoosterPackIds.get(packId);
        itemIdToItem.put(bi.getId(), bi);
      }
    } catch (Exception e) {
      log.error("error creating a map of booster item ids to booster items.", e);
    }
  }
  
  private static void setStaticBoosterItemIdsToBoosterItems() {
    log.debug("setting static map of boosterItemIds to boosterItems");

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, BoosterItem> boosterItemIdsToBoosterItemsTemp = new HashMap<Integer, BoosterItem>();
          while(rs.next()) {  //should only be one
            BoosterItem boosterItem = convertRSRowToBoosterItem(rs);
            if (boosterItem != null)
              boosterItemIdsToBoosterItemsTemp.put(boosterItem.getId(), boosterItem);
          }
          boosterItemIdsToBoosterItems = boosterItemIdsToBoosterItemsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticBoosterItemIdsToBoosterItems();
    setStaticBoosterItemIdsToBoosterItemsForBoosterPackIds();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static BoosterItem convertRSRowToBoosterItem(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int boosterPackId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int quantity = rs.getInt(i++);
    boolean isSpecial = rs.getBoolean(i++);

    BoosterItem boosterItem = new BoosterItem(id, boosterPackId, equipId, quantity, isSpecial);
    return boosterItem;
  }
}
