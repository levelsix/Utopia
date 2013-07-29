package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BattleDetails;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BattleDetailsRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_BATTLE_HISTORY;

  public static List<BattleDetails> getMostRecentBattleDetailsForDefenderAfterTime(int defenderId, int limit, Timestamp earliestBattleNotificationTimeToRetrieve) {
    log.debug("retrieving most recent battle details posts for " + defenderId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.BATTLE_HISTORY__DEFENDER_ID, defenderId);
    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
    greaterThanParams.put(DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, earliestBattleNotificationTimeToRetrieve);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, limit, greaterThanParams);
    List<BattleDetails> battleDetailsList = convertRSToBattleDetailsList(rs);
    DBConnection.get().close(rs, null, conn);
    return battleDetailsList;
  }
  
  private static List<BattleDetails> convertRSToBattleDetailsList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<BattleDetails> battleDetailsList = new ArrayList<BattleDetails>();
        while(rs.next()) {
          BattleDetails battleDetails = convertRSRowToBattleDetails(rs);
          battleDetailsList.add(battleDetails);
        }
        return battleDetailsList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }
  
  private static BattleDetails convertRSRowToBattleDetails(ResultSet rs) throws SQLException {
    int i = 1;
    int attackerId = rs.getInt(i++);
    int defenderId = rs.getInt(i++);
    BattleResult result = BattleResult.valueOf(rs.getInt(i++));
    Date battleCompleteTime = new Date(rs.getTimestamp(i++).getTime());
    
    int coinsStolen = rs.getInt(i++);
    if (rs.wasNull()) {
      coinsStolen = ControllerConstants.NOT_SET;
    }
    
    int equipStolen = rs.getInt(i++);
    if (rs.wasNull()) {
      equipStolen = ControllerConstants.NOT_SET;
    }
    
    int expGained = rs.getInt(i++);
    if (rs.wasNull()) {
      expGained = ControllerConstants.NOT_SET;
    }
    
    int stolenEquipLevel = rs.getInt(i++);
    if (rs.wasNull()) {
      stolenEquipLevel = ControllerConstants.NOT_SET;
    }

    return new BattleDetails(attackerId, defenderId, result, battleCompleteTime, coinsStolen, equipStolen, expGained, stolenEquipLevel);
  }
}
