package com.lvl6.retrieveutils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.utilmethods.StringUtils;

@Component @DependsOn("gameServer") public class UserRetrieveUtils {

  private Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private final String TABLE_NAME = DBConstants.TABLE_USER;

  private final int BATTLE_INITIAL_LEVEL_RANGE = 4;    //even number makes it more consistent. ie 6 would be +/- 3 levels from user level
  private final int BATTLE_INITIAL_RANGE_INCREASE = 2;    //even number better again
  private final int BATTLE_RANGE_INCREASE_MULTIPLE = 2;
  private final int MAX_BATTLE_DB_HITS = 5;
  private final int EXTREME_MAX_BATTLE_DB_HITS = 30;
  
  public List<User> getMentees(List<Integer> blackList, Date lastLoginAfterNow,
      int limit, boolean isGood) {
    List<Object> values = new ArrayList<Object>();
    
    String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
        DBConstants.USER__IS_MENTOR + "=? AND " + DBConstants.USER__IS_FAKE +
        "=?"; 
    values.add(false);
    values.add(false);
    
    if (isGood) {
      int amount = 3;
      query += " AND " + DBConstants.USER__TYPE + " in (";
      List<String> clauses = Collections.nCopies(amount, "?");
      query += StringUtils.getListInString(clauses, ",") + ")";
      values.add(UserType.GOOD_ARCHER_VALUE);
      values.add(UserType.GOOD_MAGE_VALUE);
      values.add(UserType.GOOD_WARRIOR_VALUE);
    } else {
      int amount = 3;
      query += " AND " + DBConstants.USER__TYPE + " in (";
      List<String> clauses = Collections.nCopies(amount, "?");
      query += StringUtils.getListInString(clauses, ",") + ")";
      values.add(UserType.BAD_ARCHER_VALUE);
      values.add(UserType.BAD_MAGE_VALUE);
      values.add(UserType.BAD_WARRIOR_VALUE);
    }
    
    if (null != blackList && !blackList.isEmpty()) {
      query += " AND " + DBConstants.USER__ID + "NOT IN (";
      int amount = blackList.size();
      List<String> clauses = Collections.nCopies(amount, "?");
      query += StringUtils.getListInString(clauses, ",") + ")";
      values.addAll(blackList);
    }
    
    if (null != lastLoginAfterNow) {
      query += " AND " + DBConstants.USER__LAST_LOGIN + " > ?";
      values.add(new Timestamp(lastLoginAfterNow.getTime()));
    }
    
    query += " ORDER BY " + DBConstants.USER__CREATE_TIME + " DESC " +
    		"LIMIT " + limit;
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
    List<User> usersList = convertRSToUsers(rs);
    DBConnection.get().close(rs, null, conn);
    return usersList;
  }
  
  public List<User> getAllMentors() {
    log.debug("retrieving users that are mentors ");

    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.USER__IS_MENTOR, true);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, DBConstants.TABLE_USER);
    List<User> usersList = convertRSToUsers(rs);
    DBConnection.get().close(rs, null, conn);
    return usersList;
    
  }
  
  public int numAccountsForUDID(String udid) {
    List<Object> params = new ArrayList<Object>();
    params.add(udid);
    Connection conn = DBConnection.get().getConnection();
    String query = "select count(*) from " +
    		TABLE_NAME + " where udid like concat(\"%\", ?, \"%\");";
    ResultSet rs = DBConnection.get().selectDirectQueryNaive(conn, query, params);
    int count = 0;
    try {
      if (null != rs) {
        try {
          if (rs.first()) {
            count = rs.getInt(1);
          }
        } catch (SQLException e) {
          log.error("sql query wrong", e);
        }
      }
    } catch (Exception e) {
      log.error("sql query wrong 2", e);
    } finally {
      DBConnection.get().close(null, null, conn);
    }
    return count;
  }
  
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
				
			}
		  }
	  }catch(Exception e) {
		  
	  }finally {
		DBConnection.get().close(null, null, conn);
	  }
	  log.warn("No users found when counting users for isFake="+isFake);
	  return 0;
  }
  
  
  ////@Cacheable(value="usersCache")
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

  public List<User> getUsersByClanId(int clanId) {
    log.debug("retrieving users with clanId " + clanId);

    Map<String, Object> absoluteConditionParams = new HashMap<String, Object>();
    absoluteConditionParams.put(DBConstants.USER__CLAN_ID, clanId);

    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = DBConnection.get().selectRowsAbsoluteAnd(conn, absoluteConditionParams, DBConstants.TABLE_USER);
    List<User> usersList = convertRSToUsers(rs);
    DBConnection.get().close(rs, null, conn);
    return usersList;
  }

  public List<User> getUsers(List<UserType> requestedTypes, int numUsers, int playerLevel, int userId, boolean guaranteeNum, 
      Double latLowerBound, Double latUpperBound, Double longLowerBound, Double longUpperBound, boolean forBattle, 
      boolean realPlayersOnly, boolean fakePlayersOnly, boolean offlinePlayersOnly, boolean prestigePlayersOnly,
      boolean inactiveShield, List<Integer> forbiddenPlayerIds) {
    log.debug("retrieving list of users for user " + userId + " with requested types " + requestedTypes + 
        " , " + numUsers + " users " + " around player level " + playerLevel + ", guaranteeNum="+guaranteeNum + 
        ", latLowerBound=" + latLowerBound + ", latUpperBound=" + latUpperBound + 
        ", longLowerBound=" + longLowerBound + ", longUpperBound=" + longUpperBound + ", forBattle=" + forBattle);

    //when there was a map in AoC, players displayed were +- 3,
    //hence use of -1 and +1
    int levelMin = Math.max(playerLevel - BATTLE_INITIAL_LEVEL_RANGE/2 - 1, 2);
    int levelMax = playerLevel + BATTLE_INITIAL_LEVEL_RANGE/2 + 1;

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
    
    if (realPlayersOnly) {
      query += DBConstants.USER__IS_FAKE + "=? and ";
      values.add(0);
      if (offlinePlayersOnly) {
        query += DBConstants.USER__LAST_LOGOUT + " > " +
            DBConstants.USER__LAST_LOGIN + " and ";
      }
      
    } else if (fakePlayersOnly) {
      query += DBConstants.USER__IS_FAKE + "=? and ";
      values.add(1);
    } 
    
    if (prestigePlayersOnly) {
      query += DBConstants.USER__PRESTIGE_LEVEL + ">? and ";
      values.add(0);
    }
    
    if (inactiveShield) {
      query += DBConstants.USER__HAS_ACTIVE_SHIELD + "=? and ";
      values.add(false);
    }

    query += DBConstants.USER__LEVEL + ">=? and " + DBConstants.USER__LEVEL + "<=? ";
    values.add(levelMin);
    values.add(levelMax);

    query += "order by " + DBConstants.USER__IS_FAKE + ", rand() limit ?";
    values.add(numUsers);

    int rangeIncrease = BATTLE_INITIAL_RANGE_INCREASE;
    int numDBHits = 1;

//    log.info("\t\t\t userRetrieveUtils.getUsers() query=" + query +
//        "\t\t values=" + values);
    
    Connection conn = DBConnection.get().getConnection();
    ResultSet rs = null;
    if (conn != null) {
      rs = DBConnection.get().selectDirectQueryNaive(conn, query, values);
      //this is in case there aren't enough users to satisfy caller's requested number of users
      //so level range is widened and db requeried
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
        log.error("problem with database call.", e);
        
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
        log.error("problem with database call.", e);
        
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
        log.error("problem with database call.", e);
        
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
    
    int clanId = rs.getInt(i++);
    if (rs.wasNull()) {
      clanId = ControllerConstants.NOT_SET;
    }
    
    Date lastGoldmineRetrieval = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastGoldmineRetrieval = new Date(ts.getTime());
    }

    Date lastMktNotificationTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastMktNotificationTime = new Date(ts.getTime());
    }

    Date lastWallNotificationTime = null;
    ts = rs.getTimestamp(i++);
    if (!rs.wasNull()) {
      lastWallNotificationTime = new Date(ts.getTime());
    }
    
    int kabamNaid = rs.getInt(i++);

    boolean hasReceivedfbReward = rs.getBoolean(i++);

    int weaponTwoEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      weaponTwoEquippedUserEquipId = ControllerConstants.NOT_SET;
    }
    int armorTwoEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      armorTwoEquippedUserEquipId = ControllerConstants.NOT_SET;
    }
    int amuletTwoEquippedUserEquipId = rs.getInt(i++);
    if (rs.wasNull()) {
      amuletTwoEquippedUserEquipId = ControllerConstants.NOT_SET;
    } 
    
    int prestigeLevel = rs.getInt(i++);

    int numAdditionalForgeSlots = rs.getInt(i++);
    int numBeginnerSalesPurchased = rs.getInt(i++);
    boolean isMentor = rs.getBoolean(i++);
    boolean hasActiveShield = rs.getBoolean(i++);
    
    User user = new User(userId, name, level, type, attack, defense, stamina, lastStaminaRefillTime, energy, lastEnergyRefillTime, 
        skillPoints, energyMax, staminaMax, diamonds, coins, marketplaceDiamondsEarnings, marketplaceCoinsEarnings, 
        vaultBalance, experience, tasksCompleted, battlesWon, battlesLost, flees,
        referralCode, numReferrals, udid, userLocation, numPostsInMarketplace, numMarketplaceSalesUnredeemed, 
        weaponEquippedUserEquipId, armorEquippedUserEquipId, amuletEquippedUserEquipId, lastLoginTime, lastLogoutTime, deviceToken, 
        lastBattleNotificationTime, lastTimeAttacked, numBadges, lastShortLicensePurchaseTime, lastLongLicensePurchaseTime, isFake, userCreateTime, 
        isAdmin, apsalarId, numCoinsRetrievedFromStructs, numAdcolonyVideosWatched, numTimesKiipRewarded, numConsecutiveDaysPlayed, 
        numGroupChatsRemaining, clanId, lastGoldmineRetrieval, lastMktNotificationTime, lastWallNotificationTime, kabamNaid, hasReceivedfbReward,
        weaponTwoEquippedUserEquipId, armorTwoEquippedUserEquipId, amuletTwoEquippedUserEquipId, prestigeLevel, numAdditionalForgeSlots, 
        numBeginnerSalesPurchased, isMentor, hasActiveShield);
    return user;
  }
}
