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

import com.lvl6.info.LockBoxItem;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.EquipClassType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class LockBoxItemRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItems;

  private static final String TABLE_NAME = DBConstants.TABLE_LOCK_BOX_ITEMS;

  public static Map<Integer, LockBoxItem> getLockBoxItemIdsToLockBoxItems() {
    log.debug("retrieving all lockBoxItems data map");
    if (lockBoxItemIdsToLockBoxItems == null) {
      setStaticLockBoxItemIdsToLockBoxItems();
    }
    return lockBoxItemIdsToLockBoxItems;
  }

  public static LockBoxItem getLockBoxItemForLockBoxItemId(int lockBoxItemId) {
    log.debug("retrieve lockBoxItem data for lockBoxItem " + lockBoxItemId);
    if (lockBoxItemIdsToLockBoxItems == null) {
      setStaticLockBoxItemIdsToLockBoxItems();      
    }
    return lockBoxItemIdsToLockBoxItems.get(lockBoxItemId);
  }

  public static List<LockBoxItem> getLockBoxItemsForLockBoxEvent(int lockBoxEventId, UserType type) {
    log.debug("retrieve lockBoxItem data for lockBoxEvent " + lockBoxEventId);
    if (lockBoxItemIdsToLockBoxItems == null) {
      setStaticLockBoxItemIdsToLockBoxItems();      
    }
    List<LockBoxItem> toreturn = new ArrayList<LockBoxItem>();
    
    EquipClassType classType = null;
    if (type == UserType.BAD_ARCHER || type == UserType.GOOD_ARCHER) classType = EquipClassType.ARCHER;
    else if (type == UserType.BAD_WARRIOR || type == UserType.GOOD_WARRIOR) classType = EquipClassType.WARRIOR;
    else if (type == UserType.BAD_MAGE || type == UserType.GOOD_MAGE) classType = EquipClassType.MAGE;
    
    for (LockBoxItem item : lockBoxItemIdsToLockBoxItems.values()) {
      if (item.getLockBoxEventId() == lockBoxEventId && (item.getClassType() == classType || item.getClassType() == EquipClassType.ALL_AMULET)) {
        toreturn.add(item);
      }
    }
    return toreturn;
  }

  private static void setStaticLockBoxItemIdsToLockBoxItems() {
    log.debug("setting static map of lockBoxItemIds to lockBoxItems");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, LockBoxItem> lockBoxItemIdsToLockBoxItemsTemp = new HashMap<Integer, LockBoxItem>();
          while(rs.next()) {  //should only be one
            LockBoxItem lockBoxItem = convertRSRowToLockBoxItem(rs);
            if (lockBoxItem != null)
              lockBoxItemIdsToLockBoxItemsTemp.put(lockBoxItem.getId(), lockBoxItem);
          }
          lockBoxItemIdsToLockBoxItems = lockBoxItemIdsToLockBoxItemsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticLockBoxItemIdsToLockBoxItems();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static LockBoxItem convertRSRowToLockBoxItem(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int lockBoxEventId = rs.getInt(i++);
    float chanceToUnlock = rs.getFloat(i++);
    String name = rs.getString(i++);
    EquipClassType type = EquipClassType.valueOf(rs.getInt(i++));
    String imageName = rs.getString(i++);

    LockBoxItem lockBoxItem = new LockBoxItem(id, lockBoxEventId, chanceToUnlock, name, type, imageName);
    return lockBoxItem;
  }
}
