package com.lvl6.retrieveutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.UserEquip;
import com.lvl6.properties.DBConstants;
import com.lvl6.utils.DBConnection;

@Component @DependsOn("gameServer") public class UserEquipRetrieveUtils {

  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER_EQUIP;

  
  @Cacheable(value="userEquipsForUser", key="#userId")
  public List<UserEquip> getUserEquipsForUser(int userId) {
    log.debug("retrieving user equips for userId " + userId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsByUserId(userId, TABLE_NAME);
    List<UserEquip> userEquips = convertRSToUserEquips(rs);
    DBConnection.get().close(rs, null, conn);
    return userEquips;
  }

  
  @Cacheable(value="equipsToUserEquipsForUser", key="#userId")
  public Map<Integer, UserEquip> getEquipIdsToUserEquipsForUser(int userId) {
    log.debug("retrieving user equips for userId " + userId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsByUserId(userId, TABLE_NAME);
    Map<Integer, UserEquip> equipIdsToUserEquips = convertRSToEquipIdsToUserEquips(rs);
    DBConnection.get().close(rs, null, conn);
    return equipIdsToUserEquips;
  }

  
  //@Cacheable(value="specificUserEquip", key="#userId+':'+#equipId")
  public UserEquip getSpecificUserEquip(int userId, int equipId) {
    log.debug("retrieving user equip for userId " + userId + " and equipId " + equipId);
    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_EQUIP__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    
    Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, paramsToVals, TABLE_NAME);
    UserEquip userEquip = convertRSSingleToUserEquips(rs);
    DBConnection.get().close(rs, null, conn);
    return userEquip;
  }

  /*
  //returns map from userId to his equipments
  public Map<Integer, List<UserEquip>> getUserEquipsForUserIds(List<Integer> userIds) {
    log.debug("retrieving user equips for userIds " + userIds);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    for (Integer i : userIds) {
      paramsToVals.put(DBConstants.USER_EQUIP__USER_ID, i); BUG BUG IT OVERWRITES
    }
    return convertRSToUserToUserEquips(DBConnection.selectRowsAbsoluteOr(paramsToVals, TABLE_NAME));
  }

    private Map<Integer, List<UserEquip>> convertRSToUserToUserEquips(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<UserEquip>> userToUserEquips = new HashMap<Integer, List<UserEquip>>();
        while(rs.next()) {  //should only be one
          UserEquip ue = convertRSRowToUserEquip(rs);
          List<UserEquip> userUEs = userToUserEquips.get(ue.getUserId());
          if (userUEs != null) {
            userUEs.add(ue);
          } else {
            List<UserEquip> ues = new ArrayList<UserEquip>();
            ues.add(ue);
            userToUserEquips.put(ue.getUserId(), ues);
          }
        }
        return userToUserEquips;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }
   */

  private Map<Integer, UserEquip> convertRSToEquipIdsToUserEquips(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, UserEquip> equipIdsToUserEquips = new HashMap<Integer, UserEquip>();
        while(rs.next()) {
          UserEquip userEquip = convertRSRowToUserEquip(rs);
          equipIdsToUserEquips.put(userEquip.getEquipId(), userEquip);
        }
        return equipIdsToUserEquips;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private List<UserEquip> convertRSToUserEquips(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<UserEquip> userEquips = new ArrayList<UserEquip>();
        while(rs.next()) {  //should only be one
          userEquips.add(convertRSRowToUserEquip(rs));
        }
        return userEquips;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private UserEquip convertRSSingleToUserEquips(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          return convertRSRowToUserEquip(rs);
        }
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
  private UserEquip convertRSRowToUserEquip(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    int quantity = rs.getInt(i++);
    UserEquip userEquip = new UserEquip(userId, equipId, quantity);
    return userEquip;
  }

}
