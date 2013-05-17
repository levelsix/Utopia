package com.lvl6.retrieveutils;

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

import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class EquipEnhancementFeederRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_EQUIP_ENHANCEMENT_FEEDERS;

  public static List<EquipEnhancementFeeder> getEquipEnhancementFeedersForEquipEnhancementId(int equipEnhancementId) {
    log.debug("retrieving equip enhancement feeders for equip enhancement id " + equipEnhancementId);
    
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.EQUIP_ENHANCEMENT_FEEDERS__EQUIP_ENHANCEMENT_ID, equipEnhancementId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, TABLE_NAME);
    List<EquipEnhancementFeeder> equipEnhancementFeeders = convertRSToEquipEnhancementFeeders(rs);
    DBConnection.get().close(rs, null, conn);
    return equipEnhancementFeeders;
  }
  
  private static List<EquipEnhancementFeeder> convertRSToEquipEnhancementFeeders(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<EquipEnhancementFeeder> equipEnhancementFeederList = new ArrayList<EquipEnhancementFeeder>();
        while(rs.next()) {
          EquipEnhancementFeeder anEquipEnhancementFeeder = convertRSRowToEquipEnhancementFeeder(rs);
          equipEnhancementFeederList.add(anEquipEnhancementFeeder);
        }
        return equipEnhancementFeederList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return null;
  }

  
  private static EquipEnhancementFeeder convertRSRowToEquipEnhancementFeeder(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int equipEnhancementId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int equipLevel = rs.getInt(i++);
    int enhancementPercentageBeforeEnhancement = rs.getInt(i++);
    
    return new EquipEnhancementFeeder(id, equipEnhancementId, equipId, equipLevel, enhancementPercentageBeforeEnhancement);
  }
}
