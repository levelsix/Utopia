package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class BlacksmithAttemptRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_BLACKSMITH;
//
//  public static List<BlacksmithAttempt> getMostRecentBlacksmithAttemptForDefenderAfterTime(int defenderId, int limit, Timestamp earliestBattleNotificationTimeToRetrieve) {
//    log.debug("retrieving most recent battle details posts for " + defenderId);
//    
//    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
//    absoluteParams.put(DBConstants.BATTLE_HISTORY__DEFENDER_ID, defenderId);
//    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
//    greaterThanParams.put(DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, earliestBattleNotificationTimeToRetrieve);
//    
//    Connection conn = DBConnection.get().getConnection();
//    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.BATTLE_HISTORY__DEFENDER_ID, limit, greaterThanParams);
//    List<BlacksmithAttempt> blacksmithAttemptList = convertRSToBlacksmithAttemptList(rs);
//    DBConnection.get().close(rs, null, conn);
//    return blacksmithAttemptList;
//  }
//  
//  private static List<BlacksmithAttempt> convertRSToBlacksmithAttemptList(ResultSet rs) {
//    if (rs != null) {
//      try {
//        rs.last();
//        rs.beforeFirst();
//        List<BlacksmithAttempt> blacksmithAttemptList = new ArrayList<BlacksmithAttempt>();
//        while(rs.next()) {
//          BlacksmithAttempt blacksmithAttempt = convertRSRowToBlacksmithAttempt(rs);
//          blacksmithAttemptList.add(blacksmithAttempt);
//        }
//        return blacksmithAttemptList;
//      } catch (SQLException e) {
//        log.error("problem with database call.");
//        log.error(e);
//      }
//    }
//    return null;
//  }
//  
//  private static BlacksmithAttempt convertRSRowToBlacksmithAttempt(ResultSet rs) throws SQLException {
//    int i = 1;
//    int attackerId = rs.getInt(i++);
//    int defenderId = rs.getInt(i++);
//    BattleResult result = BattleResult.valueOf(rs.getInt(i++));
//    Date battleCompleteTime = new Date(rs.getTimestamp(i++).getTime());
//    
//    int coinsStolen = rs.getInt(i++);
//    if (rs.wasNull()) {
//      coinsStolen = ControllerConstants.NOT_SET;
//    }
//    
//    int equipStolen = rs.getInt(i++);
//    if (rs.wasNull()) {
//      equipStolen = ControllerConstants.NOT_SET;
//    }
//    
//    int expGained = rs.getInt(i++);
//    if (rs.wasNull()) {
//      expGained = ControllerConstants.NOT_SET;
//    }
//
//    return new BlacksmithAttempt(attackerId, defenderId, result, battleCompleteTime, coinsStolen, equipStolen, expGained);
//  }
}
