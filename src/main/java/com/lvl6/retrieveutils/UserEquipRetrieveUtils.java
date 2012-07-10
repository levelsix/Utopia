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
    
    //Connection conn = DBConnection.get().connectionManager.get();
    ResultSet rs = DBConnection.get().selectRowsByUserId(userId, TABLE_NAME);
    List<UserEquip> userEquips = convertRSToUserEquips(rs);
    DBConnection.get().close(rs, null);
    return userEquips;
  }

  
  @Cacheable(value="equipsToUserEquipsForUser", key="#userId")
  public Map<Integer, List<UserEquip>> getEquipIdsToUserEquipsForUser(int userId) {
    log.debug("retrieving map of equip id to userequips for userId " + userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsByUserId(userId, TABLE_NAME);
    Map<Integer, List<UserEquip>> equipIdsToUserEquips = convertRSToEquipIdsToUserEquips(rs);
    DBConnection.get().close(rs, null);
    return equipIdsToUserEquips;
  }

  @Cacheable(value="specificUserEquip", key="#userEquipId")
  public UserEquip getSpecificUserEquip(int userEquipId) {
    log.debug("retrieving user equip for userEquipId: " + userEquipId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById( userEquipId, TABLE_NAME);
    UserEquip userEquip = convertRSSingleToUserEquips(rs);
    DBConnection.get().close(rs, null);
    return userEquip;
  }
  
  @Cacheable(value="userEquipsWithEquipId", key="#userId+':'+#equipId")
  public List<UserEquip> getUserEquipsWithEquipId(int userId, int equipId) {
    log.debug("retrieving user equip for user: " + userId + ", equipId: " + equipId);

    TreeMap <String, Object> paramsToVals = new TreeMap<String, Object>();
    paramsToVals.put(DBConstants.USER_EQUIP__USER_ID, userId);
    paramsToVals.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(paramsToVals, TABLE_NAME);
    List<UserEquip> userEquips = convertRSToUserEquips(rs);
    DBConnection.get().close(rs, null);
    return userEquips;
  }

  private Map<Integer, List<UserEquip>> convertRSToEquipIdsToUserEquips(
      ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, List<UserEquip>> equipIdsToUserEquips = new HashMap<Integer, List<UserEquip>>();
        while(rs.next()) {
          UserEquip userEquip = convertRSRowToUserEquip(rs);
          List<UserEquip> userEquipsForEquipId = equipIdsToUserEquips.get(userEquip.getEquipId());
          if (userEquipsForEquipId != null) {
            userEquipsForEquipId.add(userEquip);
          } else {
            List<UserEquip> userEquips = new ArrayList<UserEquip>();
            userEquips.add(userEquip);
            equipIdsToUserEquips.put(userEquip.getEquipId(), userEquips);
          }
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
    int userEquipId = rs.getInt(i++);
    int userId = rs.getInt(i++);
    int equipId = rs.getInt(i++);
    UserEquip userEquip = new UserEquip(userEquipId, userId, equipId);
    return userEquip;
  }

}
