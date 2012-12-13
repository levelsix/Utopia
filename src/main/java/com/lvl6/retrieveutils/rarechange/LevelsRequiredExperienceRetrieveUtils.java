package com.lvl6.retrieveutils.rarechange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class LevelsRequiredExperienceRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    if (level > ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER) {
      return levelsToRequiredExperienceForLevels.get(ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER)*2;
    }
    return levelsToRequiredExperienceForLevels.get(level);
  }

  public static void reload() {
    setStaticLevelsToRequiredExperienceForLevels();
  }

  private static void setStaticLevelsToRequiredExperienceForLevels() {
    log.debug("setting static map of levels to required experience for levels");

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectWholeTable(conn, TABLE_NAME);
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
          log.error("problem with database call.", e);
          
        }
      }
    }
    DBConnection.get().close(rs, null, conn);
  }
}
