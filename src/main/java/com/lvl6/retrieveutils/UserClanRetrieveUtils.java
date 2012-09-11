package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserClan;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserClanRetrieveUtils {

  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_CLANS;

  public List<Integer> getUserIdsRelatedToClan(int clanId) {
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_CLANS__CLAN_ID, clanId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    List<Integer> userIds = grabUserIdsFromRS(rs);
    DBConnection.get().close(rs, null, conn);
    return userIds;
  }


  private List<Integer> grabUserIdsFromRS(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<Integer> userIds = new ArrayList<Integer>();
        while(rs.next()) {
          UserClan uc = convertRSRowToUserClan(rs);
          userIds.add(uc.getUserId());
        }
        return userIds;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
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
  //        log.error("problem with database call.");
  //        log.error(e);
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
  //        log.error("problem with database call.");
  //        log.error(e);
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

    return new UserClan(userId, clanId, status);
  }

}
