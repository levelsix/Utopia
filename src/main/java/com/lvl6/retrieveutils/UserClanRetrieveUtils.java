package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserClan;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserClanRetrieveUtils {

  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS;
  
  public List<Integer> getUsersRelatedToClan(int clanId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserClan> userClans = convertRSToUserClans(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }
  
  
  @Cacheable(value="incompleteUserClansForUser", key="#userId")
  public List<UserClan> getIncompleteUserClansForUser(int userId) {
    log.debug("retrieving incomplete user quests for userId " + userId);
    
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_COMPLETE, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserClan> userClans = convertRSToUserClans(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }
  
  
  @Cacheable(value="unredeemedAndRedeemedUserClansForUser", key="#userId")
  public List<UserClan> getUnredeemedAndRedeemedUserClansForUser(int userId) {
    log.debug("retrieving unredeemed and redeemed user quests for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<UserClan> userClans = convertRSToUserClans(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }
  
  
  @Cacheable(value="unredeemedUserClansForUser", key="#userId")
  public List<UserClan> getUnredeemedUserClansForUser(int userId) {
    log.debug("retrieving unredeemed user quests for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserClan> userClans = convertRSToUserClans(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }
  
  
  @Cacheable(value="userClansForUser", key="#userId")
  public List<UserClan> getUserClansForUser(int userId) {
    log.debug("retrieving user quests for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<UserClan> userClans = convertRSToUserClans(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }
  
  public UserClan getSpecificUnredeemedUserClan(int userId, int questId) {
    log.debug("retrieving specific unredeemed user quest for userid " + userId + " and questId " + questId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__QUEST_ID, questId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserClan userClan = convertRSToSingleUserClan(rs);
    DBConnection.get().close(rs, null, conn);
    return userClan;
  }
  
  private UserClan convertRSToSingleUserClan(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          return convertRSRowToUserClan(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private List<UserClan> convertRSToUserClans(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserClan> userClans = new ArrayList<UserClan>();
        while(rs.next()) {
          userClans.add(convertRSRowToUserClan(rs));
        }
        return userClans;
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
  private UserClan convertRSRowToUserClan(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int questId = rs.getInt(i++);
    boolean isRedeemed = rs.getBoolean(i++);
    boolean isComplete = rs.getBoolean(i++);
    boolean tasksComplete = rs.getBoolean(i++);
    boolean defeatTypeJobsComplete = rs.getBoolean(i++);
    int coinsRetrievedForReq = rs.getInt(i++);
    
    UserClan userClan = new UserClan(userId, questId, isRedeemed, isComplete, tasksComplete, 
        defeatTypeJobsComplete, coinsRetrievedForReq);
    return userClan;
  }
  
}
