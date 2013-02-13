package com.lvl6.scriptsjava.csvequipreplacement;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;

import com.lvl6.info.Task;
import com.lvl6.properties.DBConstants;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.DBConnection;

public class CSVEquipReplacement {
  private static String csvFileName = "src/main/java/com/lvl6/scriptsjava/csvequipreplacement/weaponchanges.csv";

  public static void main(String[] args) {
    ApplicationContext context = new FileSystemXmlApplicationContext("target/utopia-server-1.0-SNAPSHOT/WEB-INF/spring-application-context.xml");
    DBConnection.get().init();
    
    CSVReader reader;
    try {
      reader = new CSVReader(new FileReader(csvFileName));
      
      Map<Integer, Integer> dict = new TreeMap<Integer, Integer>();
      
      String [] nextLine;
      // Skip first line
      reader.readNext();
      while ((nextLine = reader.readNext()) != null) {
//        0:1, 1:1, 2:Rusty Dagger, 3:Common, 4:Weapon, 5:x, 6:, 7:120, 8:1, 9:Cracked Bow, 10:Common, 11:Weapon, 12:, 13:, 14:185, 15:1, 16:Tree Branch, 17:Common, 18:Weapon, 19:, 
        String warriorId = nextLine[0];
        String archerId = nextLine[7];
        String mageId = nextLine[14];
        ArrayList<Object> ids = new ArrayList<Object>();
        ids.add(Integer.parseInt(warriorId));
        ids.add(Integer.parseInt(archerId));
        ids.add(Integer.parseInt(mageId));
        
        int prevails = 0;
        if (nextLine[5].length() > 0) {
          prevails = 0;
        } else if (nextLine[12].length() > 0) {
          prevails = 1;
        } else if (nextLine[19].length() > 0) {
          prevails = 2;
        }
        
        int endResult = (Integer)ids.get(prevails);
        ids.remove(prevails);
        ids.add(0, endResult);
        System.out.println("Replacing "+ids.subList(1, 3)+" with "+endResult);
        dict.put((Integer)ids.get(0), (Integer)ids.get(0));
        dict.put((Integer)ids.get(1), (Integer)ids.get(0));
        dict.put((Integer)ids.get(2), (Integer)ids.get(0));

//        String query = "update "+DBConstants.TABLE_USER_EQUIP+" set equip_id=? where equip_id in (?, ?)";
//        int rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated user_equip: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_MARKETPLACE+" set posted_equip_id=? where posted_equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated marketplace: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_BLACKSMITH+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated blacksmith: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_BLACKSMITH_HISTORY+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated blacksmith history: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated enhancement: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated enhancement feeders: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT_HISTORY+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated enhancement history: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS_HISTORY+" set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated enhancement feeders history: "+rowsUpdated+" rows.");
//
//        query = "update "+DBConstants.TABLE_BATTLE_HISTORY+" set equip_stolen=? where equip_stolen in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated battle history: "+rowsUpdated+" rows.");
//
//        query = "update diamond_equip_purchase_history set equip_id=? where equip_id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Updated enhancement: "+rowsUpdated+" rows.");
//
//        ids.remove(0);
//        query = "delete from "+DBConstants.TABLE_EQUIPMENT+" where id in (?, ?)";
//        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
//        System.out.println("Deleted equipment: "+rowsUpdated+" rows.");
      }
      
      // Update tasks potential loot
      for (Task t : TaskRetrieveUtils.getTaskIdsToTasks().values()) {
        List<Integer> loot = t.getPotentialLootEquipIds();
        if (loot != null && loot.size() > 0) {
          System.out.println("Task "+t.getId()+" prior loot equips: "+loot);
          boolean needsChange = false;
          String newLoot = "";
          for (Integer l : loot) {
            Integer newId = dict.get(l);
            if (newId == null) newId = l;
            if (!newId.equals(l)) {
              needsChange = true;
            }
            newLoot += newId + ", ";
          }

          newLoot = newLoot.substring(0, newLoot.length()-2);
          System.out.println("Needs change: "+needsChange+" new loot: "+newLoot);
          if (needsChange) {
            ArrayList<Object> x = new ArrayList<Object>();
            x.add(newLoot);
            x.add(t.getId());
            String query = "update "+DBConstants.TABLE_TASKS+" set potential_loot_equip_ids=? where id=?";
            int rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, x);
            System.out.println("Updated tasks: "+rowsUpdated+" rows.");
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
