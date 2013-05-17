package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.GoldSale;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class GoldSaleRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, GoldSale> goldSaleIdsToGoldSales;

  private static final String TABLE_NAME = DBConstants.TABLE_GOLD_SALES;

  public static Map<Integer, GoldSale> getGoldSaleIdsToGoldSales() {
    log.debug("retrieving all goldSales data map");
    if (goldSaleIdsToGoldSales == null) {
      setStaticGoldSaleIdsToGoldSales();
    }
    return goldSaleIdsToGoldSales;
  }
  
  public static List<GoldSale> getCurrentAndFutureGoldSales() {
    log.debug("retrieve current and future gold sales");
    if (goldSaleIdsToGoldSales == null) {
      setStaticGoldSaleIdsToGoldSales();
    }
    List<GoldSale> sales = new ArrayList<GoldSale>();

    long curTime = new Date().getTime();
    for (GoldSale event : goldSaleIdsToGoldSales.values()) {
      // Send all events that are not yet over
      if (event.getEndDate().getTime() > curTime) {
        sales.add(event);
      }
    }
    return sales;
  }

  public static GoldSale getGoldSaleForGoldSaleId(int goldSaleId) {
    log.debug("retrieve goldSale data for goldSale " + goldSaleId);
    if (goldSaleIdsToGoldSales == null) {
      setStaticGoldSaleIdsToGoldSales();      
    }
    return goldSaleIdsToGoldSales.get(goldSaleId);
  }

  public static Map<Integer, GoldSale> getGoldSalesForGoldSaleIds(List<Integer> ids) {
    log.debug("retrieve goldSale data for goldSaleids " + ids);
    if (goldSaleIdsToGoldSales == null) {
      setStaticGoldSaleIdsToGoldSales();      
    }
    Map<Integer, GoldSale> toreturn = new HashMap<Integer, GoldSale>();
    for (Integer id : ids) {
      toreturn.put(id,  goldSaleIdsToGoldSales.get(id));
    }
    return toreturn;
  }

  private static void setStaticGoldSaleIdsToGoldSales() {
    log.debug("setting static map of goldSaleIds to goldSales");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          HashMap<Integer, GoldSale> goldSaleIdsToGoldSalesTemp = new HashMap<Integer, GoldSale>();
          while(rs.next()) {  //should only be one
            GoldSale goldSale = convertRSRowToGoldSale(rs);
            if (goldSale != null)
              goldSaleIdsToGoldSalesTemp.put(goldSale.getId(), goldSale);
          }
          goldSaleIdsToGoldSales = goldSaleIdsToGoldSalesTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticGoldSaleIdsToGoldSales();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static GoldSale convertRSRowToGoldSale(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    Date startDate = new Date(rs.getTimestamp(i++).getTime());
    Date endDate = new Date(rs.getTimestamp(i++).getTime());
    String goldShoppeImageName = rs.getString(i++);
    String goldBarImageName = rs.getString(i++);
    String package1SaleIdentifier = rs.getString(i++);
    String package2SaleIdentifier = rs.getString(i++);
    String package3SaleIdentifier = rs.getString(i++);
    String package4SaleIdentifier = rs.getString(i++);
    String package5SaleIdentifier = rs.getString(i++);
    String packageS1SaleIdentifier = rs.getString(i++);
    String packageS2SaleIdentifier = rs.getString(i++);
    String packageS3SaleIdentifier = rs.getString(i++);
    String packageS4SaleIdentifier = rs.getString(i++);
    String packageS5SaleIdentifier = rs.getString(i++);
    
    GoldSale goldSale = new GoldSale(id, startDate, endDate, goldShoppeImageName, goldBarImageName, package1SaleIdentifier, 
        package2SaleIdentifier, package3SaleIdentifier, package4SaleIdentifier, package5SaleIdentifier, packageS1SaleIdentifier, 
        packageS2SaleIdentifier, packageS3SaleIdentifier, packageS4SaleIdentifier, packageS5SaleIdentifier, false);
    return goldSale;
  }
}
