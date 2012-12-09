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

import org.apache.log4j.Logger;

import com.lvl6.info.ClanTower;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

public class ClanTowerRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_TOWERS;

  public static ClanTower getClanTower(int towerId) {
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, towerId, TABLE_NAME);
    ClanTower clanTower = convertRSToSingleClanTower(rs);
    DBConnection.get().close(rs, null, conn);
    return clanTower;
  }

  public static List<ClanTower> getAllClanTowers() {
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, null, TABLE_NAME);
    List<ClanTower> clanTowers = convertRSToClanTowersList(rs);
    DBConnection.get().close(rs, null, conn);
    return clanTowers;
  }

  public static Map<Integer, ClanTower> getClanTowersForClanTowerIds(List<Integer> ids) {
    if(null != ids && 0 < ids.size()) {
      Connection conn = DBConnection.get().getConnection();
      Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
      String tablename = DBConstants.TABLE_CLAN_TOWERS;

      for(Integer id : ids) {
        absoluteConditionParams.put(DBConstants.CLAN_TOWERS__TOWER_ID, id);
      }

      ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(
          conn, absoluteConditionParams, tablename);
      Map<Integer, ClanTower> clanTowerIdsToClanTowers = convertRSToClanTowerIdsToClanTowers(rs);
      DBConnection.get().close(rs, null, conn);
      return clanTowerIdsToClanTowers;
    }
    return null;
  }

  /*
   * Gets all the towers with:
   * 1) the specified owner and attacker id
   * 2) the specified owner id
   * 3) the specified attacker id
   * 4) the specified owner id, and also include the towers with the specified
   * attacker id
   */
  public static List<ClanTower> getAllClanTowersWithSpecificOwnerAndOrAttackerId(
      int ownerId, int attackerId, boolean ownerAndAttackerAreEnemies){
    Connection conn = DBConnection.get().getConnection();

    Map<String, Object> absoluteConditionParams = new HashMap<String,Object>();

    if (ControllerConstants.NOT_SET != ownerId) {
      absoluteConditionParams.put(
          DBConstants.CLAN_TOWERS__CLAN_OWNER_ID, ownerId);
    }
    if (ControllerConstants.NOT_SET != attackerId) {
      absoluteConditionParams.put(
          DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID, attackerId);
    }	  

    //convert return values to objects
    ResultSet rs = null;
    if (ownerAndAttackerAreEnemies) {
      rs = DBConnection.get().selectRowsAbsoluteAnd(
          conn, absoluteConditionParams, TABLE_NAME);
    }
    else {
      rs = DBConnection.get().selectRowsAbsoluteOr(
          conn, absoluteConditionParams, TABLE_NAME);
    }
    List<ClanTower> clanTowers = convertRSToClanTowersList(rs);
    DBConnection.get().close(rs, null, conn);

    return clanTowers;
  }

  private static ClanTower convertRSToSingleClanTower(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          ClanTower clanTower = convertRSRowToClanTower(rs);
          return clanTower;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static List<ClanTower> convertRSToClanTowersList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<ClanTower> clanTowersList = new ArrayList<ClanTower>();
        while(rs.next()) {
          ClanTower clanTower = convertRSRowToClanTower(rs);
          clanTowersList.add(clanTower);
        }
        return clanTowersList;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static Map<Integer, ClanTower> convertRSToClanTowerIdsToClanTowers(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, ClanTower> clanIdsToClanTowers = new HashMap<Integer, ClanTower>();
        while(rs.next()) {
          ClanTower clanTower = convertRSToSingleClanTower(rs);
          int clanTowerId = clanTower.getId();
          if (!clanIdsToClanTowers.containsKey(clanTowerId) ) {
            clanIdsToClanTowers.put(clanTowerId, clanTower);
          } else {
            log.error("more than one clan tower for an id");
          }
        }
        return clanIdsToClanTowers;
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
  private static ClanTower convertRSRowToClanTower(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    String towerName = rs.getString(i++);
    String towerImageName = rs.getString(i++);
    int silverReward = rs.getInt(i++);
    int goldReward = rs.getInt(i++);
    int numHrsToCollect = rs.getInt(i++);
    int clanOwnerId = rs.getInt(i++);

    Date ownedStartTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      ownedStartTime = new Date(ts.getTime());
    }

    int clanAttackerId = rs.getInt(i++);

    Date attackStartTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      attackStartTime = new Date(ts.getTime());
    }

    int ownerBattleWins = rs.getInt(i++);
    int attackerBattleWins = rs.getInt(i++);
    int numberOfHoursForBattle = rs.getInt(i++);
    Date lastRewardGiven = null;
    if (!rs.wasNull()) {
      lastRewardGiven = new Date(ts.getTime());
    }

    int blue = rs.getInt(i++);
    int green = rs.getInt(i++);
    int red = rs.getInt(i++);

    return new ClanTower(id, towerName, towerImageName, clanOwnerId, ownedStartTime, silverReward, 
        goldReward, numHrsToCollect, clanAttackerId, attackStartTime, ownerBattleWins, attackerBattleWins, 
        numberOfHoursForBattle, lastRewardGiven, blue, green, red);
  }
}
