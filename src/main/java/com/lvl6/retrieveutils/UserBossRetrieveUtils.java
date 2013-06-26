package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserBoss;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserBossRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_USER_BOSSES;

  public static UserBoss getSpecificUserBoss(int userId, int bossId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_BOSSES__BOSS_ID, bossId);
    paramsToVals.put(DBConstants.USER_BOSSES__USER_ID, userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserBoss userBoss = grabUserBossFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userBoss;
  }

  public static List<UserBoss> getUserBossesForUserId(int userId,
      boolean livingBossesOnly) {
    
    Connection conn = DBConnection.get().getConnection();
    List<String> columns = null; //all columns
    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    Map<String, Object> relativeGreaterThanConditionParams =
        new HashMap<String, Object>();
    Map<String, Object> relativeLessThanConditionParams = null;
    Map<String, Object> likeCondParams = null;
    String tablename = TABLE_NAME;
    String conddelim = "AND";
    String orderByColumn = null;
    boolean orderByAsc = false;
    int limit = ControllerConstants.NOT_SET;
    boolean random = false;
    
    absoluteConditionParams.put(DBConstants.USER_BOSSES__USER_ID, userId);
    if (livingBossesOnly) {
      relativeGreaterThanConditionParams.put(
          DBConstants.USER_BOSSES__CUR_HEALTH, 0);
    }
    
    ResultSet rs = DBConnection.get().selectRows(conn, columns,
        absoluteConditionParams, relativeGreaterThanConditionParams,
        relativeLessThanConditionParams, likeCondParams,
        tablename, conddelim, orderByColumn, orderByAsc, limit, random);
    List<UserBoss> userBosses = grabUserBossesFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userBosses;
  }


  private static UserBoss grabUserBossFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserBoss uc = convertRSRowToUserBoss(rs);
          return uc;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static List<UserBoss> grabUserBossesFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserBoss> userBosses = new ArrayList<UserBoss>();
        while(rs.next()) {
          UserBoss uc = convertRSRowToUserBoss(rs);
          userBosses.add(uc);
        }
        return userBosses;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private static UserBoss convertRSRowToUserBoss(ResultSet rs) throws SQLException {
    int i = 1;
    int bossId = rs.getInt(i++);
    int userId = rs.getInt(i++);

    Date startTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      startTime = new Date(ts.getTime());
    }
    
    int curHealth = rs.getInt(i++);
    int currentLevel = rs.getInt(i++);

    
    return new UserBoss(bossId, userId, curHealth, currentLevel, startTime);
  }

}
