package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class BuildStructJobRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, BuildStructJob> buildStructJobIdsToBuildStructJobs;

  private static final String TABLE_NAME = DBConstants.TABLE_JOBS_BUILD_STRUCT;

  public static Map<Integer, BuildStructJob> getBuildStructJobIdsToBuildStructJobs() {
    log.debug("retrieving all build struct job data");
    if (buildStructJobIdsToBuildStructJobs == null) {
      setStaticBuildStructJobIdsToBuildStructJobs();
    }
    return buildStructJobIdsToBuildStructJobs;
  }

  public static Map<Integer, BuildStructJob> getBuildStructJobsForBuildStructJobIds(List<Integer> ids) {
    log.debug("retrieving map of build struct jobs with ids " + ids);
    if (buildStructJobIdsToBuildStructJobs == null) {
      setStaticBuildStructJobIdsToBuildStructJobs();
    }
    Map<Integer, BuildStructJob> toreturn = new HashMap<Integer, BuildStructJob>();
    for (Integer id : ids) {
      toreturn.put(id,  buildStructJobIdsToBuildStructJobs.get(id));
    }
    return toreturn;
  }

  public static BuildStructJob getBuildStructJobForBuildStructJobId(int buildStructJobId) {
    log.debug("retrieving build struct job data for build struct job id " + buildStructJobId);
    if (buildStructJobIdsToBuildStructJobs == null) {
      setStaticBuildStructJobIdsToBuildStructJobs();
    }
    return buildStructJobIdsToBuildStructJobs.get(buildStructJobId);
  }

  private static void setStaticBuildStructJobIdsToBuildStructJobs() {
    log.debug("setting static map of build struct job id to build struct job");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);

      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, BuildStructJob> buildStructJobIdsToBuildStructJobsTemp = new HashMap<Integer, BuildStructJob>();
          while(rs.next()) {  //should only be one
            BuildStructJob bsj = convertRSRowToBuildStructJob(rs);
            if (bsj != null)
              buildStructJobIdsToBuildStructJobsTemp.put(bsj.getId(), bsj);
          }
          buildStructJobIdsToBuildStructJobs = buildStructJobIdsToBuildStructJobsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }  
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticBuildStructJobIdsToBuildStructJobs();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static BuildStructJob convertRSRowToBuildStructJob(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int structId = rs.getInt(i++);
    int quantity = rs.getInt(i++);
    return new BuildStructJob(id, structId, quantity);
  }
}
