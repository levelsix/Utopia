package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto.DefeatTypeJobEnemyType;
import com.lvl6.utils.DBConnection;

public class DefeatTypeJobRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, DefeatTypeJob> defeatTypeJobIdsToDefeatTypeJobs;

  private static final String TABLE_NAME = DBConstants.TABLE_JOBS_DEFEAT_TYPE;

  public static Map<Integer, DefeatTypeJob> getDefeatTypeJobIdsToDefeatTypeJobs() {
    log.debug("retrieving all defeat type job data");
    if (defeatTypeJobIdsToDefeatTypeJobs == null) {
      setStaticDefeatTypeJobIdsToDefeatTypeJobs();
    }
    return defeatTypeJobIdsToDefeatTypeJobs;
  }

  public static Map<Integer, DefeatTypeJob> getDefeatTypeJobsForDefeatTypeJobIds(List<Integer> ids) {
    log.debug("retrieving map of defeat type jobs with ids " + ids);
    if (defeatTypeJobIdsToDefeatTypeJobs == null) {
      setStaticDefeatTypeJobIdsToDefeatTypeJobs();
    }
    Map<Integer, DefeatTypeJob> toreturn = new HashMap<Integer, DefeatTypeJob>();
    for (Integer id : ids) {
      toreturn.put(id,  defeatTypeJobIdsToDefeatTypeJobs.get(id));
    }
    return toreturn;
  }

  public static DefeatTypeJob getDefeatTypeJobForDefeatTypeJobId(int defeatTypeJobId) {
    log.debug("retrieving defeat type job data for defeat type job id " + defeatTypeJobId);
    if (defeatTypeJobIdsToDefeatTypeJobs == null) {
      setStaticDefeatTypeJobIdsToDefeatTypeJobs();
    }
    return defeatTypeJobIdsToDefeatTypeJobs.get(defeatTypeJobId);
  }

  private static void setStaticDefeatTypeJobIdsToDefeatTypeJobs() {
    log.debug("setting static map of defeat type job ids to defeat type jobs");

    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, DefeatTypeJob> defeatTypeJobIdsToDefeatTypeJobsTemp = new HashMap<Integer, DefeatTypeJob>();
          while(rs.next()) {  //should only be one
            DefeatTypeJob dtj = convertRSRowToDefeatTypeJob(rs);
            if (dtj != null)
              defeatTypeJobIdsToDefeatTypeJobsTemp.put(dtj.getId(), dtj);
          }
          defeatTypeJobIdsToDefeatTypeJobs = defeatTypeJobIdsToDefeatTypeJobsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }    
    }
    DBConnection.close(rs, null, conn);
  }

  public static void reload() {
    setStaticDefeatTypeJobIdsToDefeatTypeJobs();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static DefeatTypeJob convertRSRowToDefeatTypeJob(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    DefeatTypeJobEnemyType enemyType = DefeatTypeJobEnemyType.valueOf(rs.getInt(i++));
    int numEnemiesToDefeat = rs.getInt(i++);
    int cityId = rs.getInt(i++);
    return new DefeatTypeJob(id, enemyType, numEnemiesToDefeat, cityId);
  }
}
