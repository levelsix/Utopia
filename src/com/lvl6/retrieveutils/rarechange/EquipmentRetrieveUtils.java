package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.Equipment;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.utils.DBConnection;

public class EquipmentRetrieveUtils {
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Equipment> equipIdToEquipment;
  
  private static final String TABLE_NAME = DBConstants.TABLE_EQUIPMENT;
    
  public static Map<Integer, Equipment> getEquipmentIdsToEquipment() {
    log.info("retrieving equipment data");
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    return equipIdToEquipment;
  }
  
  public static List<Equipment> getAllArmoryEquipmentForClassType(ClassType classtype) {
    log.info("retrieving equipment data");
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    List <Equipment> equips = new ArrayList<Equipment>();
    for (Integer equipId : equipIdToEquipment.keySet()) {
      Equipment equip = equipIdToEquipment.get(equipId);
      if (equip.getClassType() == classtype && (equip.getDiamondPrice() > 0 || equip.getCoinPrice() > 0)) {
        equips.add(equip);
      }
    }
    return equips;
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
        Map <Integer, Equipment> equipIdToEquipmentTemp = new HashMap<Integer, Equipment>();
        while(rs.next()) {  //should only be one
          Equipment equip = convertRSRowToEquipment(rs);
          if (equip != null)
            equipIdToEquipmentTemp.put(equip.getId(), equip);
        }
        equipIdToEquipment = equipIdToEquipmentTemp;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }    
  }
  
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Equipment convertRSRowToEquipment(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    String name = rs.getString(i++);
    EquipType type = EquipType.valueOf(rs.getInt(i++));
    int attackBoost = rs.getInt(i++);
    int defenseBoost = rs.getInt(i++);
    int minLevel = rs.getInt(i++);
    int coinPrice = rs.getInt(i++);
    int diamondPrice = rs.getInt(i++);
    float chanceOfLoss = rs.getFloat(i++);
    ClassType classType = ClassType.valueOf(rs.getInt(i++));
    Rarity rarity = Rarity.valueOf(rs.getInt(i++));
    Equipment equip = null;
    if (coinPrice > 0) {
      equip = new Equipment(id, name, type, attackBoost, defenseBoost, minLevel, coinPrice, Equipment.NOT_SET, chanceOfLoss, classType, rarity);
    } else {  //this should mean diamondPrice > 0.
      equip = new Equipment(id, name, type, attackBoost, defenseBoost, minLevel, Equipment.NOT_SET, diamondPrice, chanceOfLoss, classType, rarity);      
    }
    return equip;
  }
}
