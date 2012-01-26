package com.lvl6.retrieveutils.rarechange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.jobs.MarketplaceJob;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.MarketplaceJobRequirementType;
import com.lvl6.utils.DBConnection;

public class MarketplaceJobRetrieveUtils {
  
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, MarketplaceJob> marketplaceJobIdsToMarketplaceJobs;
  
  private static final String TABLE_NAME = DBConstants.TABLE_JOBS_MARKETPLACE;

  public static Map<Integer, MarketplaceJob> getMarketplaceJobIdsToMarketplaceJobs() {
    log.info("retrieving all marketplace job data");
    if (marketplaceJobIdsToMarketplaceJobs == null) {
      setStaticMarketplaceJobIdsToMarketplaceJobs();
    }
    return marketplaceJobIdsToMarketplaceJobs;
  }
  
  public static Map<Integer, MarketplaceJob> getMarketplaceJobsForMarketplaceJobIds(List<Integer> ids) {
    log.info("retrieving marketplace jobs with ids " + ids);
    if (marketplaceJobIdsToMarketplaceJobs == null) {
      setStaticMarketplaceJobIdsToMarketplaceJobs();
    }
    Map<Integer, MarketplaceJob> toreturn = new HashMap<Integer, MarketplaceJob>();
    for (Integer id : ids) {
        toreturn.put(id,  marketplaceJobIdsToMarketplaceJobs.get(id));
    }
    return toreturn;
  }
  
  public static MarketplaceJob getMarketplaceJobForMarketplaceJobId(int marketplaceJobId) {
    log.info("retrieving marketplace job data for marketplacee job id " + marketplaceJobId);
    if (marketplaceJobIdsToMarketplaceJobs == null) {
      setStaticMarketplaceJobIdsToMarketplaceJobs();
    }
    return marketplaceJobIdsToMarketplaceJobs.get(marketplaceJobId);
  }
  
  private static void setStaticMarketplaceJobIdsToMarketplaceJobs() {
    log.info("setting static map of marketplace job id to marketplace job");
    ResultSet rs = DBConnection.selectWholeTable(TABLE_NAME);
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map <Integer, MarketplaceJob> marketplaceJobIdsToMarketplaceJobsTemp = new HashMap<Integer, MarketplaceJob>();
        while(rs.next()) {  //should only be one
          MarketplaceJob mj = convertRSRowToMarketplaceJob(rs);
          if (mj != null)
            marketplaceJobIdsToMarketplaceJobsTemp.put(mj.getId(), mj);
        }
        marketplaceJobIdsToMarketplaceJobs = marketplaceJobIdsToMarketplaceJobsTemp;
      } catch (SQLException e) {
        System.out.println("problem with database call.");
        e.printStackTrace();
      }
    }    
    
  }
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static MarketplaceJob convertRSRowToMarketplaceJob(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    MarketplaceJobRequirementType actionReq = MarketplaceJobRequirementType.valueOf(rs.getInt(i++));    
    return new MarketplaceJob(id, actionReq);
  }
}
