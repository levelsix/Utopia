package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.ClanTowerHistory;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanTowerHistoryRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLAN_TOWERS_HISTORY;

  public static ClanTowerHistory getMostRecentClanTowerHistoryForTower(int clanId, int towerId, int limit) {
    String query = "select * from "+TABLE_NAME+" where "+DBConstants.CLAN_TOWERS_HISTORY__TOWER_ID+"=? "
        +" and "+DBConstants.CLAN_TOWERS_HISTORY__WINNER_ID+" is not null and ("+ DBConstants.CLAN_TOWERS_HISTORY__OWNER_CLAN_ID
        +"=? or "+DBConstants.CLAN_TOWERS_HISTORY__ATTACKER_CLAN_ID+"=?) order by "+DBConstants.CLAN_TOWERS_HISTORY__TIME_OF_ENTRY
        +" desc limit ?";
    
    List<Object> values = new ArrayList<Object>();
    values.add(towerId);
    values.add(clanId);
    values.add(clanId);
    values.add(limit);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    ClanTowerHistory clanTowerHistory = convertRSToClanTowerHistory(rs);
    DBConnection.get().close(rs, null, conn);
    return clanTowerHistory;
  }

  private static ClanTowerHistory convertRSToClanTowerHistory(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        ClanTowerHistory marketplaceTransaction = null;
        if (rs.next()) {
          marketplaceTransaction = convertRSRowToClanTowerHistory(rs);
        }
        return marketplaceTransaction;
      } catch (SQLException e) {
        log.error("problem with database call.", e);

      }
    }
    return null;
  }

  private static ClanTowerHistory convertRSRowToClanTowerHistory(ResultSet rs) throws SQLException {
    int i = 1;
    int clanOwnerId = rs.getInt(i++);
    int clanAttackerId = rs.getInt(i++);
    int towerId = rs.getInt(i++);

    Date attackStartTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      attackStartTime = new Date(ts.getTime());
    }

    int winnerId = rs.getInt(i++);
    int ownerBattleWins = rs.getInt(i++);
    int attackerBattleWins = rs.getInt(i++);
    int numHoursForBattle = rs.getInt(i++);

    Date lastRewardGiven = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastRewardGiven = new Date(ts.getTime());
    }

    Date timeOfEntry = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      timeOfEntry = new Date(ts.getTime());
    }

    String reasonForEntry = rs.getString(i++);

    return new ClanTowerHistory(clanOwnerId, clanAttackerId, towerId, attackStartTime, winnerId, 
        ownerBattleWins, attackerBattleWins, numHoursForBattle, lastRewardGiven, timeOfEntry, reasonForEntry);
  }
}
