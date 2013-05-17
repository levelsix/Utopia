package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.ClanTowerUser;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanTowerUserRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_TOWER_USERS;

  public static List<ClanTowerUser> getClanTowerUsersForBattleId(int battleId) {
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLAN_TOWER_USERS__BATTLE_ID, battleId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteParams, TABLE_NAME);
    List<ClanTowerUser> clanTowerUserList = convertRSToClanTowerUserList(rs);
    DBConnection.get().close(rs, null, conn);
    return clanTowerUserList;
  }

  private static List<ClanTowerUser> convertRSToClanTowerUserList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<ClanTowerUser> clanTowerUserList = new ArrayList<ClanTowerUser>();
        while (rs.next()) {
          clanTowerUserList.add(convertRSRowToClanTowerUser(rs));
        }
        return clanTowerUserList;
      } catch (SQLException e) {
        log.error("problem with database call.", e);

      }
    }
    return null;
  }

  private static ClanTowerUser convertRSRowToClanTowerUser(ResultSet rs) throws SQLException {
    int i = 1;
    int battleId = rs.getInt(i++);
    int userId = rs.getInt(i++);
    boolean isInOwnerClan = rs.getBoolean(i++);
    int pointsGained = rs.getInt(i++);
    int pointsLost = rs.getInt(i++);

    return new ClanTowerUser(battleId, userId, pointsGained, isInOwnerClan, pointsLost);
  }
}
