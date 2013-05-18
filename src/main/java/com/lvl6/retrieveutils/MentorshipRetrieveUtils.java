package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Mentorship;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class MentorshipRetrieveUtils {
  
  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private final static String TABLE_NAME = DBConstants.TABLE_MENTORSHIPS;
  
  public static Mentorship getActiveMentorshipForMentee(int menteeId) {
    Mentorship forMentee = null;
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.MENTORSHIPS__MENTEE_ID, menteeId);
    paramsToVals.put(DBConstants.MENTORSHIPS__IS_DROPPED, false);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn,
        paramsToVals, TABLE_NAME);
    
    forMentee = convertRSToMentorship(rs);
    DBConnection.get().close(rs, null, conn);
    return forMentee;
  }
  
  public static List<Integer> getAllMenteeIds() {
    String query = "SELECT DISTINCT(" + DBConstants.MENTORSHIPS__MENTEE_ID +
        ") FROM " + DBConstants.TABLE_MENTORSHIPS + " AND " + 
        DBConstants.MENTORSHIPS__IS_DROPPED + "=?";
    List<Object> params = new ArrayList<Object>();
    params.add(false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, params);
    Set<Integer> ids = convertRSToMenteeIds(rs);
    DBConnection.get().close(rs, null, conn);
    List<Integer> returnValue = new ArrayList<Integer>();
    returnValue.addAll(ids);
    return returnValue;
  }
  
  public static Map<Integer, List<Mentorship>> getActiveMenteeIdToMentorshipsForMentorOrMentee(
      int mentorId, int menteeId) {
    
    String query = "SELECT * FROM " + TABLE_NAME + " WHERE (" + 
        DBConstants.MENTORSHIPS__MENTOR_ID + "=? OR " +
        DBConstants.MENTORSHIPS__MENTEE_ID + "=?) AND " +
        DBConstants.MENTORSHIPS__IS_DROPPED + "=?";
    List<Object> params = new ArrayList<Object>();
    params.add(mentorId);
    params.add(menteeId);
    params.add(false);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, params);
    
    Map<Integer, List<Mentorship>> returnValue = convertRSToMenteeIdsToListOfMentorships(rs);
    DBConnection.get().close(rs, null, conn);
    return returnValue;
  }
  
  public static Map<Integer, Mentorship> getActiveMenteeIdToMentorshipsForMentor(int mentorId) {
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.MENTORSHIPS__MENTOR_ID, mentorId);
    paramsToVals.put(DBConstants.MENTORSHIPS__IS_DROPPED, false);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn,
        paramsToVals, TABLE_NAME);
    
    Map<Integer, Mentorship> menteeIdsToMentorships = convertRSToMenteeIdsToMentorships(rs);
    DBConnection.get().close(rs, null, conn);
    return menteeIdsToMentorships;
  }
  
  private static Mentorship convertRSToMentorship (ResultSet rs) {
    Mentorship aMentorship = null;
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        if (rs.next()) {
          aMentorship = convertRSRowToMentorship(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    return aMentorship;
  }
  
  private static Map<Integer, Mentorship> convertRSToMenteeIdsToMentorships(ResultSet rs) {
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, Mentorship> idsToMentorships = new HashMap<Integer, Mentorship>(); 
        while (rs.next()) {
          Mentorship aMentorship = convertRSRowToMentorship(rs);
          if (null != aMentorship) {
            int menteeId = aMentorship.getMenteeId();
            idsToMentorships.put(menteeId, aMentorship);
          }
        }
        return idsToMentorships;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    //if no results found
    return new HashMap<Integer, Mentorship>();
  }
  
  private static Map<Integer, List<Mentorship>> convertRSToMenteeIdsToListOfMentorships(
      ResultSet rs) {
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<Mentorship>> idsToListOfMentorships =
            new HashMap<Integer, List<Mentorship>>();
        while (rs.next()) {
          Mentorship aMentorship = convertRSRowToMentorship(rs);
          if (null != aMentorship) {
            int key = aMentorship.getMenteeId();
            
            if (idsToListOfMentorships.containsKey(key)) {
              List<Mentorship> mentorshipList = idsToListOfMentorships.get(key);
              mentorshipList.add(aMentorship);
              continue;
            }
            List<Mentorship> newMentorshipList = new ArrayList<Mentorship>();
            newMentorshipList.add(aMentorship);
            idsToListOfMentorships.put(key, newMentorshipList);
          }
        }
        
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    //if no results found
    return new HashMap<Integer, List<Mentorship>>();
  }
  
  private static Set<Integer> convertRSToMenteeIds(ResultSet rs) {
    if (null != rs) {
      try {
        rs.last();
        rs.beforeFirst();
        Set<Integer> ids = new HashSet<Integer>();
        while (rs.next()) {
          int id = rs.getInt(DBConstants.MENTORSHIPS__MENTEE_ID);
          if (ControllerConstants.NOT_SET != id) {
            ids.add(id);
          }
        }
        return ids;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
      }
    }
    //if no results found
    return new HashSet<Integer>();
  }
  
  private static Mentorship convertRSRowToMentorship(ResultSet rs) throws SQLException {
    int i = 1;
    int id = rs.getInt(i++);
    int mentorId = rs.getInt(i++);
    int menteeId = rs.getInt(i++);
    Timestamp ts = rs.getTimestamp(i++);
    Date startTime = new Date(ts.getTime());
    
    ts = rs.getTimestamp(i++);
    Date questOneCompleteTime = null;
    if (!rs.wasNull()) {
      questOneCompleteTime = new Date(ts.getTime());
    }
    
    ts = rs.getTimestamp(i++);
    Date questTwoCompleteTime = null;
    if (!rs.wasNull()) {
      questTwoCompleteTime = new Date(ts.getTime());
    }
    
    ts = rs.getTimestamp(i++);
    Date questThreeCompleteTime = null;
    if (!rs.wasNull()) {
      questThreeCompleteTime = new Date(ts.getTime());
    }
    
    ts = rs.getTimestamp(i++);
    Date questFourCompleteTime = null;
    if (!rs.wasNull()) {
      questFourCompleteTime = new Date(ts.getTime());
    }
    
    ts = rs.getTimestamp(i++);
    Date questFiveCompleteTime = null;
    if (!rs.wasNull()) {
      questFiveCompleteTime = new Date(ts.getTime());
    }
    
    boolean isDropped = rs.getBoolean(i++);
    
    Mentorship aMentorship = new Mentorship(id, mentorId, menteeId,
        startTime, questOneCompleteTime, questTwoCompleteTime,
        questThreeCompleteTime, questFourCompleteTime,
        questFiveCompleteTime, isDropped);
    return aMentorship;
  }
  
}