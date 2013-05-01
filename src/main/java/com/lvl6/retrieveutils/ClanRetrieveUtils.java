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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Clan;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.StringUtils;

@Component @DependsOn("gameServer") public class ClanRetrieveUtils {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String TABLE_NAME = DBConstants.TABLE_CLANS;

  
  @Cacheable(value="clanWithId", key="#clanId")
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
  
  public static Map<Integer, Clan> getClansByIds(List<Integer> clanIds) {
    log.debug("retrieving clans with ids " + clanIds);
    
    if (clanIds == null || clanIds.size() <= 0 ) {
      return new HashMap<Integer, Clan>();
    }

    String query = "select * from " + TABLE_NAME + " where (";
    List<String> condClauses = new ArrayList<String>();
    List <Object> values = new ArrayList<Object>();
    for (Integer clanId : clanIds) {
      condClauses.add(DBConstants.CLANS__ID + "=?");
      values.add(clanId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    Map<Integer, Clan> clanIdToClanMap = convertRSToClanIdToClanMap(rs);
    DBConnection.get().close(rs, null, conn);
    return clanIdToClanMap;
    
  }
  
  public static List<Clan> getClansWithSimilarNameOrTag(String name, String tag) {
    log.debug("retrieving clan with name " + name);
    
    TreeMap <String, Object> likeParams = new TreeMap<String, Object>();
    likeParams.put(DBConstants.CLANS__NAME, "%"+name+"%");
    likeParams.put(DBConstants.CLANS__TAG, "%"+tag+"%");
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsLikeOr(conn, likeParams, TABLE_NAME);
    List<Clan> clans = convertRSToClansList(rs);
    DBConnection.get().close(rs, null, conn);
    return clans;
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
        log.error("problem with database call.", e);
        
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
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private static Map<Integer, Clan> convertRSToClanIdToClanMap(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Clan> clanIdsToClans = new HashMap<Integer, Clan>();
        while (rs.next()) {
          Clan c = convertRSRowToClan(rs);
          if (null != c) {
            int clanId = c.getId();
            clanIdsToClans.put(clanId, c);
          }
        }
      } catch(SQLException e) {
        log.error("problem with database call.", e);
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
    int currentTierLevel = rs.getInt(i++);
    boolean requestToJoinRequired = rs.getBoolean(i++);
    if (rs.wasNull()) {
      //for legacy clans, they were by default a request to join was required
      requestToJoinRequired = true;
    }
    
    return new Clan(clanId, name, ownerId, clanCreateTime, description, tag,
        isGood, currentTierLevel, requestToJoinRequired);
  }
}
