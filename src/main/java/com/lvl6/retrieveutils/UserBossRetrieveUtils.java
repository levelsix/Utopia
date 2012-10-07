package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Boss;
import com.lvl6.info.UserBoss;
import com.lvl6.properties.DBConstants;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserBossRetrieveUtils {

  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_BOSSES;

  public UserBoss getSpecificUserBoss(int userId, int bossId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_BOSSES__BOSS_ID, bossId);
    paramsToVals.put(DBConstants.USER_BOSSES__USER_ID, userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserBoss userClan = grabUserBossFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClan;
  }

  public List<UserBoss> getUserBossesForUserId(int userId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_BOSSES__USER_ID, userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserBoss> userBosses = grabUserBossesFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userBosses;
  }

  public List<UserBoss> getActiveUserBossesForUserId(int userId) {
    List<UserBoss> userBosses = getUserBossesForUserId(userId);
    List<UserBoss> toReturn = new ArrayList<UserBoss>();
    
    long curTime = new Date().getTime();
    for (UserBoss ub : userBosses) {
      Boss b = BossRetrieveUtils.getBossForBossId(ub.getBossId());
      
      if (ub.getCurrentHealth() > 0 && ub.getStartTime().getTime()+60000*b.getMinutesToKill() > curTime) {
        toReturn.add(ub);
      }
    }
    
    return toReturn.size() > 0 ? toReturn : null;
  }

  private UserBoss grabUserBossFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserBoss uc = convertRSRowToUserBoss(rs);
          return uc;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private List<UserBoss> grabUserBossesFromRS(ResultSet rs) {
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
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private UserBoss convertRSRowToUserBoss(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int bossId = rs.getInt(i++);

    Date startTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      startTime = new Date(ts.getTime());
    }
    
    int curHealth = rs.getInt(i++);
    int numTimesKilled = rs.getInt(i++);

    return new UserBoss(userId, bossId, curHealth, numTimesKilled, startTime);
  }

}
