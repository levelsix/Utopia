package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserClan;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserClanRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_CLANS;

  public List<UserClan> getUserClanMembersInClan(int clanId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    paramsToVals.put(DBConstants.USER_CLANS__STATUS, UserClanStatus.MEMBER.getNumber());

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserClan> userClans = grabUserClansFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }

  public List<UserClan> getUserClanMembersInClanOr(int clanId1, int clanId2) {
    String query = "select * from " + TABLE_NAME + " where (" + DBConstants.USER_CLANS__CLAN_ID +
        " = ? or " + DBConstants.USER_CLANS__CLAN_ID + " = ? ) and " + DBConstants.USER_CLANS__STATUS +
        " = ?";
    
    List<Object> values = new ArrayList<Object>();
    values.add(clanId1);
    values.add(clanId2);
    values.add(UserClanStatus.MEMBER.getNumber());

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    List<UserClan> userClans = grabUserClansFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }

  public List<UserClan> getUserClansRelatedToUser(int userId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__USER_ID, userId);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<UserClan> userClans = grabUserClansFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }

  public List<UserClan> getUserClansRelatedToClan(int clanId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__CLAN_ID, clanId);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = null;
    if (ControllerConstants.CLAN__ALLIANCE_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId ||
        ControllerConstants.CLAN__LEGION_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId) {
      //some clans will have a shit ton of people.
      //Getting 1k+ users generates buffer overflow exception
      rs = DBConnection.get().selectRowsAbsoluteAndLimit(conn, paramsToVals, TABLE_NAME,
          ControllerConstants.CLAN__ALLIANCE_LEGION_LIMIT_TO_RETRIEVE_FROM_DB);
    } else {
      rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    }
    List<UserClan> userClans = grabUserClansFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClans;
  }

  ////@Cacheable(value="specificUserClan")
  public UserClan getSpecificUserClan(int userId, int clanId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__CLAN_ID, clanId);
    paramsToVals.put(DBConstants.USER_CLANS__USER_ID, userId);

    Connection conn = DBConnection.get().getReadOnlyConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserClan userClan = grabUserClanFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userClan;
  }

  private UserClan grabUserClanFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {
          UserClan uc = convertRSRowToUserClan(rs);
          return uc;
        }
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  public List<Integer> getUserIdsRelatedToClan(int clanId) {
    List<UserClan> userClans = getUserClansRelatedToClan(clanId);
    List<Integer> userIds = new ArrayList<Integer>();
    for (UserClan userClan : userClans) {
      userIds.add(userClan.getUserId());
    }
    return userIds;
  }

  private List<UserClan> grabUserClansFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserClan> userClans = new ArrayList<UserClan>();
        while(rs.next()) {
          UserClan uc = convertRSRowToUserClan(rs);
          userClans.add(uc);
        }
        return userClans;
      } catch (SQLException e) {
        log.error("problem with database call.", e);
        
      }
    }
    return null;
  }

  //
  //  private UserClan convertRSToSingleUserClan(ResultSet rs) {
  //    if (rs != null) {
  //      try {
  //        rs.last();
  //        rs.beforeFirst();
  //        while(rs.next()) {
  //          return convertRSRowToUserClan(rs);
  //        }
  //      } catch (SQLException e) {
  //        log.error("problem with database call.", e);
  //        
  //      }
  //    }
  //    return null;
  //  }
  //
  //  private List<UserClan> convertRSToUserClans(ResultSet rs) {
  //    if (rs != null) {
  //      try {
  //        rs.last();
  //        rs.beforeFirst();
  //        List<UserClan> userClans = new ArrayList<UserClan>();
  //        while(rs.next()) {
  //          userClans.add(convertRSRowToUserClan(rs));
  //        }
  //        return userClans;
  //      } catch (SQLException e) {
  //        log.error("problem with database call.", e);
  //        
  //      }
  //    }
  //    return null;
  //  }

  /*
   * assumes the resultset is apprpriately set up. traverses the row it's on.
   */
  private UserClan convertRSRowToUserClan(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int clanId = rs.getInt(i++);
    UserClanStatus status = UserClanStatus.valueOf(rs.getInt(i++));

    Date requestTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      requestTime = new Date(ts.getTime());
    }


    return new UserClan(userId, clanId, status, requestTime);
  }

}
