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

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.StringUtils;

@Component @DependsOn("gameServer") public class UserRetrieveUtils {

  private Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER;

  private final int BATTLE_INITIAL_LEVEL_RANGE = 2;    //even number makes it more consistent. ie 6 would be +/- 3 levels from user level
  private final int BATTLE_INITIAL_RANGE_INCREASE = 2;    //even number better again
  private final int BATTLE_RANGE_INCREASE_MULTIPLE = 2;
  private final int MAX_BATTLE_DB_HITS = 5;
  private final int EXTREME_MAX_BATTLE_DB_HITS = 30;
  
  
  public Integer countUsers(Boolean isFake){
	  List<Object> params = new ArrayList<Object>();
	  params.add(isFake);
	  Connection conn = DBConnection.get().getConnection();
	  ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, "select count(*) from "+TABLE_NAME+" where is_fake = ?;", params) ;
	  try {
		  if(rs != null) {
			  Integer count;
			try {
				if(rs.first()) {
					count = rs.getInt(1);
					return count;
				}
			} catch (SQLException e) {
				log.error(e);
			}
		  }
	  }catch(Exception e) {
		  log.error(e);
	  }finally {
		DBConnection.get().close(null, null, conn);
	  }
	  log.warn("No users found when counting users for isFake="+isFake);
	  return 0;
  }
  
  
  //@Cacheable(value="usersCache")
  public User getUserById(int userId) {
    log.debug("retrieving user with userId " + userId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsById(conn, userId, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.get().close(rs, null, conn);
    return user;
  }

  public Map<Integer, User> getUsersByIds(List<Integer> userIds) {
    log.debug("retrieving users with userIds " + userIds);
    
    if (userIds == null || userIds.size() <= 0 ) {
      return new HashMap<Integer, User>();
    }

    String query = "select * from " + TABLE_NAME + " where (";
    List<String> condClauses = new ArrayList<String>();
    List <Object> values = new ArrayList<Object>();
    for (Integer userId : userIds) {
      condClauses.add(DBConstants.USER__ID + "=?");
      values.add(userId);
    }
    query += StringUtils.getListInString(condClauses, "or") + ")";

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    Map<Integer, User> userIdToUserMap = convertRSToUserIdToUsersMap(rs);
    DBConnection.get().close(rs, null, conn);
    return userIdToUserMap;
  }

  public List<User> getUsers(List<UserType> requestedTypes, int numUsers, int playerLevel, int userId, boolean guaranteeNum, 
      Double latLowerBound, Double latUpperBound, Double longLowerBound, Double longUpperBound, boolean forBattle, 
      List<Integer> forbiddenPlayerIds) {
    log.debug("retrieving list of users for user " + userId + " with requested types " + requestedTypes + 
        " , " + numUsers + " users " + " around player level " + playerLevel + ", guaranteeNum="+guaranteeNum + 
        ", latLowerBound=" + latLowerBound + ", latUpperBound=" + latUpperBound + 
        ", longLowerBound=" + longLowerBound + ", longUpperBound=" + longUpperBound + ", forBattle=" + forBattle);

    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2 - 1, 2);
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2;

    List <Object> values = new ArrayList<Object>();

    String query = "select * from " + TABLE_NAME + " where ";

    if (requestedTypes != null && requestedTypes.size() > 0) {
      query += "(";
      for (int i = 0; i < requestedTypes.size(); i++) {
        values.add(requestedTypes.get(i).getNumber());
        if (i == requestedTypes.size() - 1) {
          query += DBConstants.USER__TYPE + "=?";
        } else {
          query += DBConstants.USER__TYPE + "=?  or ";
        }
      }
      query += ") and ";
    }

    if (forbiddenPlayerIds != null && forbiddenPlayerIds.size() > 0) {
      query += "(";
      for (int i = 0; i < forbiddenPlayerIds.size(); i++) {
        values.add(forbiddenPlayerIds.get(i));
        if (i == forbiddenPlayerIds.size() - 1) {
          query += DBConstants.USER__ID + "!=?";
        } else {
          query += DBConstants.USER__ID + "!=?  and ";
        }
      }
      query += ") and ";
    }
    
    if (latLowerBound != null) {
      query += DBConstants.USER__LATITUDE + ">=? and ";
      values.add(latLowerBound);
    }
    if (latUpperBound != null) {
      query += DBConstants.USER__LATITUDE + "<=? and ";
      values.add(latUpperBound);
    }
    if (longLowerBound != null) {
      query += DBConstants.USER__LONGITUDE + ">=? and ";
      values.add(longLowerBound);
    }
    if (longUpperBound != null) {
      query += DBConstants.USER__LONGITUDE + "<=? and ";
      values.add(longUpperBound);
    }

    if (forBattle) {
      query += "(" + DBConstants.USER__LAST_TIME_ATTACKED + "<=? or " +  DBConstants.USER__LAST_TIME_ATTACKED + " is ?) and ";
      values.add(new Timestamp(new Date().getTime() - ControllerConstants.NUM_MINUTES_SINCE_LAST_BATTLE_BEFORE_APPEARANCE_IN_ATTACK_LISTS*60000));
      values.add(null);
    }

    query += DBConstants.USER__LEVEL + ">=? and " + DBConstants.USER__LEVEL + "<=? ";
    values.add(levelMin);
    values.add(levelMax);

    query += "order by " + DBConstants.USER__IS_FAKE + ", rand() limit ?";
    values.add(numUsers);

    int rangeIncrease = BATTLE_INITIAL_RANGE_INCREASE;
    int numDBHits = 1;

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
      while (rs != null && MiscMethods.getRowCount(rs) < numUsers) {
        values.remove(values.size()-1);
        values.remove(values.size()-1);
        values.remove(values.size()-1);
        levelMin = Math.max(2, levelMin - rangeIncrease/4);
        values.add(levelMin);
        levelMax = levelMax + rangeIncrease*3/4;
        values.add(levelMax);
        values.add(numUsers);
        rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
        numDBHits++;
        if (!guaranteeNum) {
          if (numDBHits == MAX_BATTLE_DB_HITS) break;
        }
        if (numDBHits == EXTREME_MAX_BATTLE_DB_HITS) break;
        rangeIncrease *= BATTLE_RANGE_INCREASE_MULTIPLE;
      }
    }
    
    List<User> users = convertRSToUsers(rs);
    if (users == null) users = new ArrayList<User>();
    
    log.debug("retrieved " + users.size() + " users in level range " + levelMin+"-"+levelMax + 
        " when " + numUsers + " around " + playerLevel + " were requested");

    
    DBConnection.get().close(rs, null, conn);
    return users;
  }

  public List<User> getUsersByReferralCodeOrName(String queryString) {
    log.debug("retrieving user with queryString " + queryString);

    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__REFERRAL_CODE, queryString);
    paramsToVals.put(DBConstants.USER__NAME, queryString);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    
    List<User> users = convertRSToUsers(rs);
    if (users == null) users = new ArrayList<User>();
    DBConnection.get().close(rs, null, conn);
    return users;
  }
  
  public User getUserByUDID(String UDID) {
    log.debug("retrieving user with udid " + UDID);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__UDID, UDID);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.get().close(rs, null, conn);
    return user;
  }

  public User getUserByReferralCode(String referralCode) {
    log.debug("retrieving user with referral code " + referralCode);
    Map <String, Object> paramsToVals = new HashMap<String, Object>();
    paramsToVals.put(DBConstants.USER__REFERRAL_CODE, referralCode);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteOr(conn, paramsToVals, TABLE_NAME);
    User user = convertRSToUser(rs);
    DBConnection.get().close(rs, null, conn);
    return user;
  }

  private User convertRSToUser(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        while(rs.next()) {  //should only be one
          return convertRSRowToUser(rs);
        }
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private Map<Integer, User> convertRSToUserIdToUsersMap(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        Map<Integer, User> userIdsToUsers = new HashMap<Integer, User>();
        while(rs.next()) {
          User user = convertRSRowToUser(rs);
          if (user != null) {
            userIdsToUsers.put(user.getId(), user);
          }
        }
        return userIdsToUsers;
      } catch (SQLException e) {
        log.error("problem with database call.");
        log.error(e);
      }
    }
    return null;
  }

  private List<User> convertRSToUsers(ResultSet rs) {
    if (rs != null) {
      try {
        rs.last();
        rs.beforeFirst();
        List<User> users = new ArrayList<User>();
        while(rs.next()) {  //should only be one
          users.add(convertRSRowToUser(rs));
        }
        return users;
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
  private User convertRSRowToUser(ResultSet rs) throws SQLException {
    int i = 1;
    int userId = rs.getInt(i++);
    String name = rs.getString(i++);
    int level = rs.getInt(i++);
    UserType type = UserType.valueOf(rs.getInt(i++));
    int attack = rs.getInt(i++);
    int defense = rs.getInt(i++);
    int stamina = rs.getInt(i++);

    Date lastStaminaRefillTime = null;
    Timestamp ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastStaminaRefillTime = new Date(ts.getTime());
    }

    int energy = rs.getInt(i++);

    Date lastEnergyRefillTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastEnergyRefillTime = new Date(ts.getTime());
    }

    int skillPoints = rs.getInt(i++);
    int energyMax = rs.getInt(i++);
    int staminaMax = rs.getInt(i++);
    int diamonds = rs.getInt(i++);
    int coins = rs.getInt(i++);
    int marketplaceDiamondsEarnings = rs.getInt(i++);
    int marketplaceCoinsEarnings = rs.getInt(i++);
    int vaultBalance = rs.getInt(i++);
    int experience = rs.getInt(i++);
    int tasksCompleted = rs.getInt(i++);
    int battlesWon = rs.getInt(i++);
    int battlesLost = rs.getInt(i++);
    int flees = rs.getInt(i++);
    String referralCode = rs.getString(i++);
    int numReferrals = rs.getInt(i++);
    String udid = rs.getString(i++);
    Location userLocation = new Location(rs.getDouble(i++), rs.getDouble(i++));
    int numPostsInMarketplace = rs.getInt(i++);
    int numMarketplaceSalesUnredeemed = rs.getInt(i++);

    int weaponEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      weaponEquippedUserEquipId = ControllerConstants.NOT_SET;
    }
    int armorEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      armorEquippedUserEquipId = ControllerConstants.NOT_SET;
    }
    int amuletEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      amuletEquippedUserEquipId = ControllerConstants.NOT_SET;
    }

    Date lastLoginTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLoginTime = new Date(ts.getTime());
    }

    Date lastLogoutTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLogoutTime = new Date(ts.getTime());
    }

    String deviceToken = rs.getString(i++);

    Date lastBattleNotificationTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastBattleNotificationTime = new Date(ts.getTime());
    }

    Date lastTimeAttacked = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastTimeAttacked = new Date(ts.getTime());
    }

    int numBadges = rs.getInt(i++);

    Date lastShortLicensePurchaseTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastShortLicensePurchaseTime = new Date(ts.getTime());
    }

    Date lastLongLicensePurchaseTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastLongLicensePurchaseTime = new Date(ts.getTime());
    }

    boolean isFake = rs.getBoolean(i++);
    
    Date userCreateTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      userCreateTime = new Date(ts.getTime());
    }

    boolean isAdmin = rs.getBoolean(i++);
    
    String apsalarId = rs.getString(i++);
    int numCoinsRetrievedFromStructs = rs.getInt(i++);
    int numAdcolonyVideosWatched = rs.getInt(i++);
    int numTimesKiipRewarded = rs.getInt(i++);
    int numConsecutiveDaysPlayed = rs.getInt(i++);
    int numGroupChatsRemaining = rs.getInt(i++);
    
    User user = new User(userId, name, level, type, attack, defense, stamina, lastStaminaRefillTime, energy, lastEnergyRefillTime, 
        skillPoints, energyMax, staminaMax, diamonds, coins, marketplaceDiamondsEarnings, marketplaceCoinsEarnings, 
        vaultBalance, experience, tasksCompleted, battlesWon, battlesLost, flees,
        referralCode, numReferrals, udid, userLocation, numPostsInMarketplace, numMarketplaceSalesUnredeemed, 
        weaponEquippedUserEquipId, armorEquippedUserEquipId, amuletEquippedUserEquipId, lastLoginTime, lastLogoutTime, deviceToken, 
        lastBattleNotificationTime, lastTimeAttacked, numBadges, lastShortLicensePurchaseTime, lastLongLicensePurchaseTime, isFake, userCreateTime, 
        isAdmin, apsalarId, numCoinsRetrievedFromStructs, numAdcolonyVideosWatched, numTimesKiipRewarded, numConsecutiveDaysPlayed, 
        numGroupChatsRemaining);
    return user;
  }
}
