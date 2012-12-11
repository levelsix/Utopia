package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserQuest;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserQuestRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private final String TABLE_NAME = DBConstants.TABLE_USER_QUESTS;
  
  //only used in script
  public List<UserQuest> getUnredeemedIncompleteUserQuests() {
    log.debug("retrieving unredeemed and incomplete user quests");
    
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_COMPLETE, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserQuest> userQuests = convertRSToUserQuests(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuests;
  }
  
  
  @Cacheable(value="incompleteUserQuestsForUser", key="#userId")
  public List<UserQuest> getIncompleteUserQuestsForUser(int userId) {
    log.debug("retrieving incomplete user quests for userId " + userId);
    
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_COMPLETE, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserQuest> userQuests = convertRSToUserQuests(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuests;
  }
  
  
  @Cacheable(value="unredeemedAndRedeemedUserQuestsForUser", key="#userId")
  public List<UserQuest> getUnredeemedAndRedeemedUserQuestsForUser(int userId) {
    log.debug("retrieving unredeemed and redeemed user quests for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<UserQuest> userQuests = convertRSToUserQuests(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuests;
  }
  
  
  @Cacheable(value="unredeemedUserQuestsForUser", key="#userId")
  public List<UserQuest> getUnredeemedUserQuestsForUser(int userId) {
    log.debug("retrieving unredeemed user quests for userId " + userId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserQuest> userQuests = convertRSToUserQuests(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuests;
  }
  
  
  @Cacheable(value="userQuestsForUser", key="#userId")
  public List<UserQuest> getUserQuestsForUser(int userId) {
    log.debug("retrieving user quests for userId " + userId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(conn, userId, TABLE_NAME);
    List<UserQuest> userQuests = convertRSToUserQuests(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuests;
  }
  
  public UserQuest getSpecificUnredeemedUserQuest(int userId, int questId) {
    log.debug("retrieving specific unredeemed user quest for userid " + userId + " and questId " + questId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_QUESTS__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_QUESTS__QUEST_ID, questId);
    paramsToVals.put(DBConstants.USER_QUESTS__IS_REDEEMED, false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserQuest userQuest = convertRSToSingleUserQuest(rs);
    DBConnection.get().close(rs, null, conn);
    return userQuest;
  }
  
  private UserQuest convertRSToSingleUserQuest(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          return convertRSRowToUserQuest(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  private List<UserQuest> convertRSToUserQuests(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserQuest> userQuests = new ArrayList<UserQuest>();
        while(rs.next()) {
          userQuests.add(convertRSRowToUserQuest(rs));
        }
        return userQuests;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }
  
  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private UserQuest convertRSRowToUserQuest(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int questId = rs.getInt(i++);
    boolean isRedeemed = rs.getBoolean(i++);
    boolean isComplete = rs.getBoolean(i++);
    boolean tasksComplete = rs.getBoolean(i++);
    boolean defeatTypeJobsComplete = rs.getBoolean(i++);
    int coinsRetrievedForReq = rs.getInt(i++);
    
    UserQuest userQuest = new UserQuest(userId, questId, isRedeemed, isComplete, tasksComplete, 
        defeatTypeJobsComplete, coinsRetrievedForReq);
    return userQuest;
  }
  
}
