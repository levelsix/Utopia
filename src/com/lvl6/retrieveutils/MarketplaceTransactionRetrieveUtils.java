package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.lvl6.info.BattleDetails;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.utils.DBConnection;

public class MarketplaceTransactionRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY;

  public static List<MarketplaceTransaction> getAllMarketplaceTransactionsAfterLastlogoutForDefender(Timestamp lastLogout, int posterId) {
    log.info("retrieving all marketplace transactions for " + posterId + " after " + lastLogout);
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID, posterId);

    TreeMap <String, Object> greaterThanParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_PURCHASE, lastLogout);
    
    return convertRSToMarketplaceTransactionsList(DBConnection.selectRowsAbsoluteAndOrderbydescGreaterthan(absoluteParams, TABLE_NAME, DBConstants.MARKETPLACE_TRANSACTION_HISTORY__TIME_OF_PURCHASE, greaterThanParams));
  }
  
  private static List<MarketplaceTransaction> convertRSToMarketplaceTransactionsList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<MarketplaceTransaction> marketplaceTransactionsList = new ArrayList<MarketplaceTransaction>();
        while(rs.next()) {
          MarketplaceTransaction marketplaceTransaction = convertRSRowToMarketplaceTransaction(rs);
          marketplaceTransactionsList.add(marketplaceTransaction);
        }
        return marketplaceTransactionsList;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
  
  
  /*
  private static MarketplaceTransaction convertRSRowToMarketplaceTransaction(ResultSet rs) throws SQLException {
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

    return new MarketplaceTransaction(attackerId, defenderId, result, battleCompleteTime, coinsStolen, equipStolen, expGained);
  }
  */
}