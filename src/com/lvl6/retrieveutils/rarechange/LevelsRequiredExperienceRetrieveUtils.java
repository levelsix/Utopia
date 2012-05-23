package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class LevelsRequiredExperienceRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static Map<Integer, Integer> levelsToRequiredExperienceForLevels;

  private static final String TABLE_NAME = DBConstants.TABLE_LEVELS_REQUIRED_EXPERIENCE;

  public static Map<Integer, Integer> getLevelsToRequiredExperienceForLevels() {
    log.debug("retrieving all exp-requirements-for-level data");
    if (levelsToRequiredExperienceForLevels == null) {
      setStaticLevelsToRequiredExperienceForLevels();
    }
    return levelsToRequiredExperienceForLevels;
  }

  public static int getRequiredExperienceForLevel(int level) {
    log.debug("retrieving exp-requirement for level " + level);
    if (levelsToRequiredExperienceForLevels == null) {
      setStaticLevelsToRequiredExperienceForLevels();
    }
    return levelsToRequiredExperienceForLevels.get(level);
  }

  public static void reload() {
    setStaticLevelsToRequiredExperienceForLevels();
  }

  private static void setStaticLevelsToRequiredExperienceForLevels() {
    log.debug("setting static map of levels to required experience for levels");

    Connection conn = DBConnection.getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.selectWholeTable(conn, TABLE_NAME);
      if (rs != null) {
        try {
          rs.last();
          rs.beforeFirst();
          Map <Integer, Integer> levelsToRequiredExperienceForLevelsTemp = new HashMap<Integer, Integer>();
          while(rs.next()) {
            int i = 1;
            levelsToRequiredExperienceForLevelsTemp.put(rs.getInt(i++), rs.getInt(i++));
          }
          levelsToRequiredExperienceForLevels = levelsToRequiredExperienceForLevelsTemp;
        } catch (SQLException e) {
          log.error("problem with database call.");
          log.error(e);
        }
      }
    }
    DBConnection.close(rs, null, conn);
  }
}
