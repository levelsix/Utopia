package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.MarketplacePost;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class MarketplaceTransactionRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY;

  public static List<MarketplaceTransaction> getMostRecentMarketplaceTransactionsForPoster(int posterId, int limit) {
    log.debug("retrieving up to " + limit + " most recent marketplace transactions posts for " + posterId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.MARKETPLACE_TRANSACTION_HISTORY__POSTER_ID, posterId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, absoluteParams, TABLE_NAME, DBConstants.MARKETPLACE_TRANSACTION_HISTORY__MARKETPLACE_ID, limit);
    List<MarketplaceTransaction> marketplacePostTransactions = convertRSToMarketplaceTransactionsList(rs);
    DBConnection.get().close(rs, null, conn);
    return marketplacePostTransactions;
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

  private static MarketplaceTransaction convertRSRowToMarketplaceTransaction(ResultSet rs) throws SQLException {
    int i = 1;
    int marketplaceId = rs.getInt(i++);
    int posterId = rs.getInt(i++);
    int buyerId = rs.getInt(i++);
    MarketplacePostType postType = MarketplacePostType.valueOf(rs.getInt(i++));
    Date timeOfPost = new Date(rs.getTimestamp(i++).getTime());
    Date timeOfPurchase = new Date(rs.getTimestamp(i++).getTime());
    int postedEquipId = rs.getInt(i++);

    int diamondCost = rs.getInt(i++);
    if (rs.wasNull()) diamondCost = ControllerConstants.NOT_SET;
    
    int coinCost = rs.getInt(i++);
    if (rs.wasNull()) coinCost = ControllerConstants.NOT_SET;

    int equipLevel = rs.getInt(i++);
    
    MarketplacePost mp = new MarketplacePost(marketplaceId, posterId, postType, timeOfPost, 
        postedEquipId, diamondCost, coinCost, equipLevel);
    
    return new MarketplaceTransaction(mp, buyerId, timeOfPurchase);
  }
}
