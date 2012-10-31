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
  
  public static List<ClanTower> getAllClanTowersWithSpecificOwnerAndAttackerId(
		  int ownerId, int attackerId){
	  Connection conn = DBConnection.get().getConnection();
	  String tableName = DBConstants.TABLE_CLAN_TOWERS;
	  
	  //filter table by the columns: clan_owner_id=ownerId, clan_attacker_id=attackerId
	  Map<String, Object> absoluteConditionParams = 
			  new HashMap<String,Object>();
	  absoluteConditionParams.put(
			  DBConstants.CLAN_TOWERS__CLAN_OWNER_ID, ownerId);
	  absoluteConditionParams.put(
			  DBConstants.CLAN_TOWERS__CLAN_ATTACKER_ID, attackerId);
	  
	  //convert return values to objects
	  ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(
			  conn, absoluteConditionParams, tableName);
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

    return new ClanTower(id, towerName, towerImageName, clanOwnerId, ownedStartTime, silverReward, goldReward, numHrsToCollect, clanAttackerId, attackStartTime, ownerBattleWins, attackerBattleWins, numberOfHoursForBattle, lastRewardGiven);
  }
}
