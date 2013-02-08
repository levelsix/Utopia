package com.lvl6.scriptsjava.csvequipreplacement;

import java.io.FileReader;
import java.util.ArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class CSVEquipReplacement {
  private static String csvFileName = "src/main/java/com/lvl6/scriptsjava/csvequipreplacement/weaponchanges.csv";

  public static void main(String[] args) {
    ApplicationContext context = new FileSystemXmlApplicationContext("target/utopia-server-1.0-SNAPSHOT/WEB-INF/spring-application-context.xml");
    DBConnection.get().init();
    
    CSVReader reader;
    try {
      reader = new CSVReader(new FileReader(csvFileName));
      
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

        String query = "update "+DBConstants.TABLE_USER_EQUIP+" set equip_id=? where equip_id in (?, ?)";
        int rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
        System.out.println("Updated user_equip: "+rowsUpdated+" rows.");

        query = "update "+DBConstants.TABLE_MARKETPLACE+" set posted_equip_id=? where posted_equip_id in (?, ?)";
        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
        System.out.println("Updated marketplace: "+rowsUpdated+" rows.");

        query = "update "+DBConstants.TABLE_BLACKSMITH+" set equip_id=? where equip_id in (?, ?)";
        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
        System.out.println("Updated blacksmith: "+rowsUpdated+" rows.");

        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT+" set equip_id=? where equip_id in (?, ?)";
        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
        System.out.println("Updated enhancement: "+rowsUpdated+" rows.");

        query = "update "+DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS+" set equip_id=? where equip_id in (?, ?)";
        rowsUpdated = DBConnection.get().updateDirectQueryNaive(query, ids);
        System.out.println("Updated enhancement feeders: "+rowsUpdated+" rows.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
