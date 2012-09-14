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

import com.lvl6.info.Clan;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class ClanRetrieveUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLANS;

  public static Clan getClanWithId(int clanId) {
    log.debug("retrieving clan with id " + clanId);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLANS__ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteParams, TABLE_NAME);
    Clan clan = convertRSToSingleClan(rs);
    DBConnection.get().close(rs, null, conn);
    return clan;
  }
  
  public static Clan getClanWithNameOrTag(String name, String tag) {
    log.debug("retrieving clan with name " + name);
    
    TreeMap <String, Object> absoluteParams = new TreeMap<String, Object>();
    absoluteParams.put(DBConstants.CLANS__NAME, name);
    absoluteParams.put(DBConstants.CLANS__TAG, tag);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn, absoluteParams, TABLE_NAME);
    Clan clan = convertRSToSingleClan(rs);
    DBConnection.get().close(rs, null, conn);
    return clan;
  }
 
  public static List<Clan> getMostRecentClansBeforeClanId(int limit, int clanId) {
    TreeMap <String, Object> lessThanParamsToVals = new TreeMap<String, Object>();
    lessThanParamsToVals.put(DBConstants.CLANS__ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimitLessthan(conn, null, TABLE_NAME, DBConstants.CLANS__ID, limit, lessThanParamsToVals);
    List<Clan> clans = convertRSToClansList(rs);
    DBConnection.get().close(rs, null, conn);
    return clans;
  }

  public static List<Clan> getMostRecentClans(int limit) {
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAndOrderbydescLimit(conn, null, TABLE_NAME, DBConstants.CLANS__ID, limit);
    List<Clan> clans = convertRSToClansList(rs);
    DBConnection.get().close(rs, null, conn);
    return clans;
  }
  
  private static Clan convertRSToSingleClan(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          Clan clan = convertRSRowToClan(rs);
          return clan;
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
  
  private static List<Clan> convertRSToClansList(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<Clan> clansList = new ArrayList<Clan>();
        while(rs.next()) {
          Clan clan = convertRSRowToClan(rs);
          clansList.add(clan);
        }
        return clansList;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private static Clan convertRSRowToClan(ResultSet rs) throws SQLException {
    int i = 1;

    int clanId = rs.getInt(i++);
    int ownerId = rs.getInt(i++);
    String name = rs.getString(i++);
    
    Date clanCreateTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      clanCreateTime = new Date(ts.getTime());
    }
    
    String description = rs.getString(i++);
    String tag = rs.getString(i++);
    boolean isGood = rs.getBoolean(i++);
    
    return new Clan(clanId, name, ownerId, clanCreateTime, description, tag, isGood);
  }
}
