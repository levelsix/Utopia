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

import com.lvl6.info.BattleDetails;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.utils.DBConnection;

public class BattleDetailsRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_BATTLE_HISTORY;

  public static List<BattleDetails> getAllBattleDetailsAfterLastlogoutForDefender(Timestamp lastLogout, int defenderId) {
    log.info("retrieving all battle details for " + defenderId + " after " + lastLogout);
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.BATTLE_HISTORY__DEFENDER_ID, defenderId);

    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
    greaterThanParams.put(DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, lastLogout);
    
    Connection conn = DBConnection.getConnection();
    ResultSet rs = DBConnection.selectRowsAbsoluteAndOrderbydescGreaterthan(conn, absoluteParams, TABLE_NAME, DBConstants.BATTLE_HISTORY__BATTLE_COMPLETE_TIME, greaterThanParams);
    List<BattleDetails> battleDetailsList = convertRSToBattleDetailsList(rs);
    DBConnection.close(rs, null, conn);
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
        log.error("problem with database call.");
        log.error(e);
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
      coinsStolen = ControllerConstants.NOT_SET;
    }
    
    int expGained = rs.getInt(i++);
    if (rs.wasNull()) {
      expGained = ControllerConstants.NOT_SET;
    }

    return new BattleDetails(attackerId, defenderId, result, battleCompleteTime, coinsStolen, equipStolen, expGained);
  }
}
