package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Equipment;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class EquipmentRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Equipment> equipIdToEquipment;

  private static final String TABLE_NAME = DBConstants.TABLE_EQUIPMENT;

  public static Map<Integer, Equipment> getEquipmentIdsToEquipment() {
    log.debug("retrieving all equipment data");
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    return equipIdToEquipment;
  }

  public static List<Equipment> getAllArmoryEquipmentForClassType(ClassType classtype) {
    log.debug("retrieving all armory equipment for class type " + classtype);
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    List <Equipment> equips = new ArrayList<Equipment>();
    for (Integer equipId : equipIdToEquipment.keySet()) {
      Equipment equip = equipIdToEquipment.get(equipId);
      if (equip.getClassType() == classtype || equip.getClassType() == ClassType.ALL_AMULET) {
        equips.add(equip);
      }
    }
    return equips;
  }

  public static Map<Integer, Equipment> getEquipmentIdsToEquipment(List<Integer> equipIds) {
    log.debug("retrieving equipment with ids " + equipIds);
    if (equipIdToEquipment == null) {
      setStaticEquipIdsToEquipment();
    }
    log.debug("equipIdToEquipment is " + equipIdToEquipment);
    Map<Integer, Equipment> toreturn = new HashMap<Integer, Equipment>();
    for (Integer equipId : equipIds) {
      toreturn.put(equipId,  equipIdToEquipment.get(equipId));
    }
    return toreturn;
  }

  private static void setStaticEquipIdsToEquipment() {
    log.debug("setting static map of equipIds to equipment");
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
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
    DBConnection.get().close(rs, null, conn);
  }
  
  public static void reload() {
    setStaticEquipIdsToEquipment();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static Equipment convertRSRowToEquipment(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    String name = rs.getString(i++);
    EquipType type = EquipType.valueOf(rs.getInt(i++));
    String description = rs.getString(i++);
    int attackBoost = rs.getInt(i++);
    int defenseBoost = rs.getInt(i++);
    int minLevel = rs.getInt(i++);
    int coinPrice = rs.getInt(i++);
    boolean coinPriceSet = !rs.wasNull();
    int diamondPrice = rs.getInt(i++);
    boolean diamondPriceSet = !rs.wasNull();
    float chanceOfLoss = rs.getFloat(i++);
    ClassType classType = ClassType.valueOf(rs.getInt(i++));
    Rarity rarity = Rarity.valueOf(rs.getInt(i++));
    boolean isBuyableInArmory = rs.getBoolean(i++);

    Equipment equip = null;
    if (coinPriceSet && !diamondPriceSet) {
      equip = new Equipment(id, name, type, description, attackBoost, defenseBoost, minLevel, coinPrice, Equipment.NOT_SET, chanceOfLoss, classType, rarity, isBuyableInArmory);
    } else if (diamondPriceSet && !coinPriceSet){
      equip = new Equipment(id, name, type, description, attackBoost, defenseBoost, minLevel, Equipment.NOT_SET, diamondPrice, chanceOfLoss, classType, rarity, isBuyableInArmory);      
    } else if (diamondPriceSet && coinPriceSet){
      log.error("equipment should only have coin or diamond price, and this one doesnt: equip with id " + id);
      return null;
    } 

    //3 types
    //1) sellable in armory, 2) not sellable in armory but sellable in marketplace, 3) never sellable
    //1) normal sword. 2) epics/legendaries. 3) bandanas

    //all equips should have either diamondCost or coinCost set to be put in the hashmap.
    //buyable in armory is now determined by flag

    //bandanas are listed in the table with coinPrice = 0 and diamondPrice = null and not buyable
    //same with epics and legendaries
    //bandanas rarity is common

    //all non epic, non legendary, need either diamondPrice > 0 or coinPrice > 0 to show up in marketplace
    //this is why bandanas cant be sold in the marketplace, but epic/legendary can be sold for gold
    //epic and legendary items not in armory

    //if the item is coinPrice = 0 but diamondPrice = null, the item can be stolen
    //if the other way around, the item cannot be stolen
    return equip;
  }
}
