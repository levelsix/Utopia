package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class PossessEquipJobRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, PossessEquipJob> possessEquipJobIdsToPossessEquipJobs;

  private static final String TABLE_NAME = DBConstants.TABLE_JOBS_POSSESS_EQUIP;

  public static Map<Integer, PossessEquipJob> getPossessEquipJobIdsToPossessEquipJobs() {
    log.debug("retrieving all possess equip job data");
    if (possessEquipJobIdsToPossessEquipJobs == null) {
      setStaticPossessEquipJobIdsToPossessEquipJobs();
    }
    return possessEquipJobIdsToPossessEquipJobs;
  }

  public static Map<Integer, PossessEquipJob> getPossessEquipJobsForPossessEquipJobIds(List<Integer> ids) {
    log.debug("retrieving possess equip jobs with ids " + ids);
    if (possessEquipJobIdsToPossessEquipJobs == null) {
      setStaticPossessEquipJobIdsToPossessEquipJobs();
    }
    Map<Integer, PossessEquipJob> toreturn = new HashMap<Integer, PossessEquipJob>();
    for (Integer id : ids) {
      toreturn.put(id,  possessEquipJobIdsToPossessEquipJobs.get(id));
    }
    return toreturn;
  }

  public static PossessEquipJob getPossessEquipJobForPossessEquipJobId(int possessEquipJobId) {
    log.debug("retrieving possess equip job for id " + possessEquipJobId);
    if (possessEquipJobIdsToPossessEquipJobs == null) {
      setStaticPossessEquipJobIdsToPossessEquipJobs();
    }
    return possessEquipJobIdsToPossessEquipJobs.get(possessEquipJobId);
  }

  private static void setStaticPossessEquipJobIdsToPossessEquipJobs() {
    log.debug("setting static map of possess equip job id to possess equip job");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, PossessEquipJob> possessEquipJobIdsToPossessEquipJobsTemp = new HashMap<Integer, PossessEquipJob>();
          while(rs.next()) {
            PossessEquipJob usj = convertRSRowToPossessEquipJob(rs);
            if (usj != null)
              possessEquipJobIdsToPossessEquipJobsTemp.put(usj.getId(), usj);
          }
          possessEquipJobIdsToPossessEquipJobs = possessEquipJobIdsToPossessEquipJobsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.", e);
          
        }
      }    
    }
    DBConnection.get().close(rs, null, conn);
  }

  public static void reload() {
    setStaticPossessEquipJobIdsToPossessEquipJobs();
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static PossessEquipJob convertRSRowToPossessEquipJob(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int quantity = rs.getInt(i++);

    return new PossessEquipJob(id, equipId, quantity);
  }
}
