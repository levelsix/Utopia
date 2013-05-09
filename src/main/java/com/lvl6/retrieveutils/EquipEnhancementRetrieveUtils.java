package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.EquipEnhancement;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class EquipEnhancementRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_EQUIP_ENHANCEMENT;

  public static List<EquipEnhancement> getEquipEnhancementsForUser(int userId) {
    log.debug("retrieving equip enhancements for user " + userId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<EquipEnhancement> equipEnhancements = convertRSToEquipEnhancements(rs);
    DBConnection.get().close(rs, null, conn);
    return equipEnhancements;
  }
  
  public static EquipEnhancement getEquipEnhancementsForEquipEnhancementId(int equipEnhancementId) {
    log.debug("retrieving equip enhancements for equip enhancement id " + equipEnhancementId);
    
    Connection conn = DBConnection.get().getReadOnlyConnection();
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.EQUIP_ENHANCEMENT__ID, equipEnhancementId);
    
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, TABLE_NAME);
    EquipEnhancement equipEnhancement = null;
    try {
      rs.last();
      rs.beforeFirst();
      if(rs.next()) {
        equipEnhancement = convertRSRowToEquipEnhancement(rs);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    DBConnection.get().close(rs, null, conn);
    return equipEnhancement;
  }
  
  private static List<EquipEnhancement> convertRSToEquipEnhancements(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<EquipEnhancement> equipEnhancementList = new ArrayList<EquipEnhancement>();
        while(rs.next()) {
          EquipEnhancement anEquipEnhancement = convertRSRowToEquipEnhancement(rs);
          equipEnhancementList.add(anEquipEnhancement);
        }
        return equipEnhancementList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  
  private static EquipEnhancement convertRSRowToEquipEnhancement(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int equipLevel = rs.getInt(i++);
    int enhancementPercentage = rs.getInt(i++);
    Date startTime = new Date(rs.getTimestamp(i++).getTime());
    
    return new EquipEnhancement(id, userId, equipId, equipLevel, enhancementPercentage,
        startTime);
  }
}
