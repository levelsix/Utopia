package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.Equipment;
import com.lvl6.info.Equipment.EquipType;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class EquipmentRetrieveUtils {
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Equipment> equipIdToEquipment;
  
  private static final String TABLE_NAME = DBConstants.TABLE_EQUIPMENT;
    
  public static Map<Integer, Equipment> getAllEquipmentIdsToEquipment() {
    log.info("retrieving equipment data");
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    return equipIdToEquipment;
  }
  
  public static Map<Integer, Equipment> getEquipmentIdsToEquipment(List<Integer> equipIds) {
    log.info("retrieving equipment with ids " + equipIds);
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    log.info("equipIdToEquipment is " + equipIdToEquipment);
    Map<Integer, Equipment> toreturn = new HashMap<Integer, Equipment>();
    for (Integer equipId : equipIds) {
        toreturn.put(equipId,  equipIdToEquipment.get(equipId));
    }
    return toreturn;
  }
  
  private static void setStaticEquipIdsToEquipment() {
    log.info("setting static map of equipIds to equipment");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        equipIdToEquipment = new HashMap<Integer, Equipment>();
        while(rs.next()) {  //should only be one
          Equipment equip = convertRSRowToEquipment(rs);
          if (equip != null)
            equipIdToEquipment.put(equip.getId(), equip);
        }
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }    
  }
  
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Equipment convertRSRowToEquipment(ResultSet rs) throws SQLException {
    int id = rs.getInt(1);
    String name = rs.getString(2);
    EquipType type = EquipType.valueOf(rs.getInt(3));
    int attackBoost = rs.getInt(4);
    int defenseBoost = rs.getInt(5);
    int minLevel = rs.getInt(6);
    int coinPrice = rs.getInt(7);
    int diamondPrice = rs.getInt(8);
    double chanceOfLoss = rs.getDouble(9);
    Equipment equip = null;
    if (coinPrice > 0) {
      equip = new Equipment(id, name, type, attackBoost, defenseBoost, minLevel, coinPrice, Equipment.NOT_SET, chanceOfLoss);
    } else {  //this should mean diamondPrice > 0.
      equip = new Equipment(id, name, type, attackBoost, defenseBoost, minLevel, Equipment.NOT_SET, diamondPrice, chanceOfLoss);      
    }
    return equip;
  }
}
