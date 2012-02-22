package com.lvl6.info;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto.StatType;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.DBConnection;

public class User {

  private int id;
  private String name;
  private int level;
  private UserType type;
  private int attack;
  private int defense;
  private int stamina;
  private Date lastStaminaRefillTime;
  private boolean isLastStaminaStateFull;
  private int energy;
  private Date lastEnergyRefillTime;
  private boolean isLastEnergyStateFull;
  private int skillPoints;
  private int healthMax;
  private int energyMax;
  private int staminaMax;
  private int diamonds;
  private int coins;
  private int marketplaceDiamondsEarnings;
  private int marketplaceCoinsEarnings;
  private int vaultBalance;
  private int experience;
  private int tasksCompleted;
  private int battlesWon;
  private int battlesLost;
  private int hourlyCoins;
  private String armyCode;
  private int numReferrals;
  private String udid;
  private Location userLocation;
  private int numPostsInMarketplace;
  private int numMarketplaceSalesUnredeemed;
  private int weaponEquipped;
  private int armorEquipped;
  private int amuletEquipped;
  private Date lastLogin;
  private Date lastLogout;
  private String deviceToken;
  private Date lastBattleNotificationTime;
  private Date lastTimeAttacked;
  private String macAddress;

  public User(int id, String name, int level, UserType type, int attack,
      int defense, int stamina, Date lastStaminaRefillTime,
      boolean isLastStaminaStateFull, int energy, Date lastEnergyRefillTime,
      boolean isLastEnergyStateFull, int skillPoints, int healthMax,
      int energyMax, int staminaMax, int diamonds, int coins,
      int marketplaceDiamondsEarnings, int marketplaceCoinsEarnings,
      int vaultBalance, int experience, int tasksCompleted, int battlesWon,
      int battlesLost, int hourlyCoins, String armyCode, int numReferrals,
      String udid, Location userLocation, int numPostsInMarketplace,
      int numMarketplaceSalesUnredeemed, int weaponEquipped, int armorEquipped,
      int amuletEquipped, Date lastLogin, Date lastLogout, String deviceToken,
      Date lastBattleNotificationTime, Date lastTimeAttacked, String macAddress) {
    this.id = id;
    this.name = name;
    this.level = level;
    this.type = type;
    this.attack = attack;
    this.defense = defense;
    this.stamina = stamina;
    this.lastStaminaRefillTime = lastStaminaRefillTime;
    this.isLastStaminaStateFull = isLastStaminaStateFull;
    this.energy = energy;
    this.lastEnergyRefillTime = lastEnergyRefillTime;
    this.isLastEnergyStateFull = isLastEnergyStateFull;
    this.skillPoints = skillPoints;
    this.healthMax = healthMax;
    this.energyMax = energyMax;
    this.staminaMax = staminaMax;
    this.diamonds = diamonds;
    this.coins = coins;
    this.marketplaceDiamondsEarnings = marketplaceDiamondsEarnings;
    this.marketplaceCoinsEarnings = marketplaceCoinsEarnings;
    this.vaultBalance = vaultBalance;
    this.experience = experience;
    this.tasksCompleted = tasksCompleted;
    this.battlesWon = battlesWon;
    this.battlesLost = battlesLost;
    this.hourlyCoins = hourlyCoins;
    this.armyCode = armyCode;
    this.numReferrals = numReferrals;
    this.udid = udid;
    this.userLocation = userLocation;
    this.numPostsInMarketplace = numPostsInMarketplace;
    this.numMarketplaceSalesUnredeemed = numMarketplaceSalesUnredeemed;
    this.weaponEquipped = weaponEquipped;
    this.armorEquipped = armorEquipped;
    this.amuletEquipped = amuletEquipped;
    this.lastLogin = lastLogin;
    this.lastLogout = lastLogout;
    this.deviceToken = deviceToken;
    this.lastBattleNotificationTime = lastBattleNotificationTime;
    this.lastTimeAttacked = lastTimeAttacked;
    this.macAddress = macAddress;
  }

  public boolean updateLastloginLastlogout(Timestamp lastLogin, Timestamp lastLogout) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastLogin == null && lastLogout == null) {
      return false;
    }
    if (lastLogin != null) {
      absoluteParams.put(DBConstants.USER__LAST_LOGIN, lastLogin);
    }
    if (lastLogout != null) {
      absoluteParams.put(DBConstants.USER__LAST_LOGOUT, lastLogout);
    }
    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (lastLogin != null) {
        this.lastLogin = lastLogin;
      }
      if (lastLogout != null) {
        this.lastLogout = lastLogout;
      }
      return true;
    }
    return false;
  }

  public boolean updateLevel(int levelChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__LEVEL, levelChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.level += levelChange;
      return true;
    }
    return false;
  }

  /*
   * used for refilling stamina
   */
  public boolean updateLaststaminarefilltimeStaminaIslaststaminastatefull(Timestamp lastStaminaRefillTime, int staminaChange, boolean isLastStaminaStateFull) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, lastStaminaRefillTime);
    absoluteParams.put(DBConstants.USER__IS_LAST_STAMINA_STATE_FULL, isLastStaminaStateFull);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__STAMINA, staminaChange);

    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.lastStaminaRefillTime = lastStaminaRefillTime;
      this.stamina += staminaChange;
      this.isLastStaminaStateFull = isLastStaminaStateFull;
      return true;
    }
    return false;
  }

  /*
   * used for refilling energy
   */
  public boolean updateLastenergyrefilltimeEnergyIslastenergystatefull(Timestamp lastEnergyRefillTime, int energyChange, boolean isLastEnergyStateFull) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, lastEnergyRefillTime);
    absoluteParams.put(DBConstants.USER__IS_LAST_ENERGY_STATE_FULL, isLastEnergyStateFull);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__ENERGY, energyChange);

    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.lastEnergyRefillTime = lastEnergyRefillTime;
      this.energy += energyChange;
      this.isLastEnergyStateFull = isLastEnergyStateFull;
      return true;
    }
    return false;
  }

  /*
   * used for using diamonds to refill stat
   */
  public boolean updateRelativeDiamondsRestoreStatChangerefilltime (int diamondChange, StatType statType, Timestamp newRefillTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (statType == StatType.ENERGY) {
      absoluteParams.put(DBConstants.USER__ENERGY, energyMax);
      absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, newRefillTime);
      absoluteParams.put(DBConstants.USER__IS_LAST_ENERGY_STATE_FULL, true);
    } else if (statType == StatType.STAMINA) {
      absoluteParams.put(DBConstants.USER__STAMINA, staminaMax);
      absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, newRefillTime);
      absoluteParams.put(DBConstants.USER__IS_LAST_STAMINA_STATE_FULL, true);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, 
        absoluteParams, conditionParams, "and");
    if (numUpdated == 1) {
      if (statType == StatType.ENERGY) {
        this.energy = energyMax;
        this.lastEnergyRefillTime = newRefillTime;
        this.isLastEnergyStateFull = true;
      } else if (statType == StatType.STAMINA) {
        this.stamina = staminaMax;
        this.lastStaminaRefillTime = newRefillTime;
        this.isLastStaminaStateFull = true;
      }
      this.diamonds += diamondChange;
      return true;
    }
    return false;
  }

  /*
   * used for using skill points
   */
  public boolean updateRelativeEnergyEnergymaxHealthmaxStaminaStaminamaxSkillPoints 
  (int energyChange, int energyMaxChange, int healthMaxChange, 
      int staminaChange, int staminaMaxChange, int skillPointsChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    if (energyChange > 0) relativeParams.put(DBConstants.USER__ENERGY, energyChange);
    if (energyMaxChange > 0) relativeParams.put(DBConstants.USER__ENERGY_MAX, energyMaxChange);
    if (healthMaxChange > 0) relativeParams.put(DBConstants.USER__HEALTH_MAX, healthMaxChange);
    if (staminaChange > 0) relativeParams.put(DBConstants.USER__STAMINA, staminaChange);
    if (staminaMaxChange > 0) relativeParams.put(DBConstants.USER__STAMINA_MAX, staminaMaxChange);

    relativeParams.put(DBConstants.USER__SKILL_POINTS, skillPointsChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.energy += energyChange;
      this.energyMax += energyMaxChange;
      this.healthMax += healthMaxChange;
      this.stamina += staminaChange;
      this.staminaMax += staminaMaxChange;
      this.skillPoints += skillPointsChange;
      return true;
    }
    return false;
  }

  /*
   * used for using skill points
   */
  public boolean updateRelativeAttackDefenseSkillPoints (int attackChange, int defenseChange, 
      int skillPointsChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    if (attackChange > 0) relativeParams.put(DBConstants.USER__ATTACK, attackChange);
    if (defenseChange > 0) relativeParams.put(DBConstants.USER__DEFENSE, defenseChange);
    relativeParams.put(DBConstants.USER__SKILL_POINTS, skillPointsChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.attack += attackChange;
      this.defense += defenseChange;
      this.skillPoints += skillPointsChange;
      return true;
    }
    return false;
  }

  /*
   * used for purchasing and selling structures, redeeming quests
   */
  public boolean updateRelativeDiamondsCoinsExperienceNaive (int diamondChange, int coinChange, 
      int experienceChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__EXPERIENCE, experienceChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += diamondChange;
      this.coins += coinChange;
      this.experience += experienceChange;
      return true;
    }
    return false;
  }


  /*
   * used for marketplace purchase
   */
  public boolean updateMoveMarketplaceEarningsToRealStatResetNummarketplacesalesunredeemed() {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__DIAMONDS, marketplaceDiamondsEarnings);
    relativeParams.put(DBConstants.USER__COINS, marketplaceCoinsEarnings);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();

    absoluteParams.put(DBConstants.USER__MARKETPLACE_DIAMONDS_EARNINGS, 0);
    absoluteParams.put(DBConstants.USER__MARKETPLACE_COINS_EARNINGS, 0);
    absoluteParams.put(DBConstants.USER__NUM_MARKETPLACE_SALES_UNREDEEMED, 0);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += marketplaceDiamondsEarnings;
      this.coins += marketplaceCoinsEarnings;
      this.marketplaceDiamondsEarnings = 0;
      this.marketplaceCoinsEarnings = 0;
      return true;
    }
    return false;
  }

  /*
   * used for marketplace purchase
   */
  public boolean updateRelativeDiamondsearningsCoinsearningsNumpostsinmarketplaceNummarketplacesalesunredeemedNaive 
  (int diamondEarningsChange, int coinEarningsChange, 
      int numPostsInMarketplaceChange, int numMarketplaceSalesUnredeemedChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__MARKETPLACE_DIAMONDS_EARNINGS, diamondEarningsChange);
    relativeParams.put(DBConstants.USER__MARKETPLACE_COINS_EARNINGS, coinEarningsChange);
    relativeParams.put(DBConstants.USER__NUM_POSTS_IN_MARKETPLACE, numPostsInMarketplaceChange);
    relativeParams.put(DBConstants.USER__NUM_MARKETPLACE_SALES_UNREDEEMED, numMarketplaceSalesUnredeemedChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.marketplaceDiamondsEarnings += diamondEarningsChange;
      this.marketplaceCoinsEarnings += coinEarningsChange;
      this.numPostsInMarketplace += numPostsInMarketplaceChange;
      this.numMarketplaceSalesUnredeemed += numMarketplaceSalesUnredeemedChange;
      return true;
    }
    return false;
  }


  /*
   * used for marketplace
   */
  public boolean updateRelativeDiamondsCoinsNumpostsinmarketplaceNaive (int diamondChange, int coinChange, 
      int numPostsInMarketplaceChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__NUM_POSTS_IN_MARKETPLACE, numPostsInMarketplaceChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.coins += coinChange;
      this.diamonds += diamondChange;
      this.numPostsInMarketplace += numPostsInMarketplaceChange;
      return true;
    }
    return false;
  }


  /*
   * used for tasks
   *        * user- coins/exp/tasks_completed increase, energy decrease
   */
  public boolean updateRelativeCoinsExpTaskscompletedEnergySimulateenergyrefill (int coinChange, int expChange, int tasksCompletedChange, 
      int energyChange, boolean simulateEnergyRefill, Timestamp clientTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__EXPERIENCE, expChange);
    relativeParams.put(DBConstants.USER__TASKS_COMPLETED, tasksCompletedChange);
    relativeParams.put(DBConstants.USER__ENERGY, energyChange);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (simulateEnergyRefill) {
      absoluteParams.put(DBConstants.USER__IS_LAST_ENERGY_STATE_FULL, false);
      absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, clientTime);
    }
    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (simulateEnergyRefill) {
        this.isLastEnergyStateFull = false;
        this.lastEnergyRefillTime = clientTime;
      }
      this.coins += coinChange;
      this.experience += expChange;
      this.tasksCompleted += tasksCompletedChange;
      this.energy += energyChange;
      return true;
    }
    return false;
  }

  /*
   * used for in app purchases, armory, finishingnormstructbuild
   */
  public boolean updateRelativeDiamondsNaive (int diamondChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    if (diamondChange != 0) {
      relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += diamondChange;
      return true;
    }
    return false;
  }

  public boolean updateRelativeCoinsNaive (int coinChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    if (coinChange != 0) {
      relativeParams.put(DBConstants.USER__COINS, coinChange);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.coins += coinChange;
      return true;
    }
    return false;
  }


  /*
   * used for vault transactions
   */
  public boolean updateRelativeCoinsVault (int coinsChange, int vaultChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    if (coinsChange != 0) relativeParams.put(DBConstants.USER__COINS, coinsChange);
    if (vaultChange != 0) relativeParams.put(DBConstants.USER__VAULT_BALANCE, vaultChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.coins += coinsChange;
      this.vaultBalance += vaultChange;
      return true;
    }
    return false;
  }

  /*
   * used for battles
   */
  public boolean updateRelativeStaminaExperienceCoinsBattleswonBattleslostSimulatestaminarefill (int stamina, int experience, 
      int coins, int battlesWon, int battlesLost, boolean simulateStaminaRefill, Timestamp clientTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    if (stamina != 0) relativeParams.put(DBConstants.USER__STAMINA, stamina);
    if (experience != 0) relativeParams.put(DBConstants.USER__EXPERIENCE, experience);
    if (coins != 0) relativeParams.put(DBConstants.USER__COINS, coins);
    if (battlesWon != 0) relativeParams.put(DBConstants.USER__BATTLES_WON, battlesWon);
    if (battlesLost != 0) relativeParams.put(DBConstants.USER__BATTLES_LOST, battlesLost);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (simulateStaminaRefill) {
      absoluteParams.put(DBConstants.USER__IS_LAST_STAMINA_STATE_FULL, false);
      absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, clientTime);
    }
    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (simulateStaminaRefill) {
        this.isLastStaminaStateFull = false;
        this.lastStaminaRefillTime = clientTime;
      }
      this.stamina += stamina;
      this.experience += experience;
      this.coins += coins;
      this.battlesWon += battlesWon;
      this.battlesLost += battlesLost;
      return true;
    }
    return false;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getLevel() {
    return level;
  }

  public UserType getType() {
    return type;
  }

  public int getAttack() {
    return attack;
  }

  public int getDefense() {
    return defense;
  }

  public int getStamina() {
    return stamina;
  }

  public Date getLastStaminaRefillTime() {
    return lastStaminaRefillTime;
  }

  public boolean isLastStaminaStateFull() {
    return isLastStaminaStateFull;
  }

  public int getEnergy() {
    return energy;
  }

  public Date getLastEnergyRefillTime() {
    return lastEnergyRefillTime;
  }

  public boolean isLastEnergyStateFull() {
    return isLastEnergyStateFull;
  }

  public int getSkillPoints() {
    return skillPoints;
  }

  public int getHealthMax() {
    return healthMax;
  }

  public int getEnergyMax() {
    return energyMax;
  }

  public int getStaminaMax() {
    return staminaMax;
  }

  public int getDiamonds() {
    return diamonds;
  }

  public int getCoins() {
    return coins;
  }

  public int getMarketplaceDiamondsEarnings() {
    return marketplaceDiamondsEarnings;
  }

  public int getMarketplaceCoinsEarnings() {
    return marketplaceCoinsEarnings;
  }

  public int getVaultBalance() {
    return vaultBalance;
  }

  public int getExperience() {
    return experience;
  }

  public int getTasksCompleted() {
    return tasksCompleted;
  }

  public int getBattlesWon() {
    return battlesWon;
  }

  public int getBattlesLost() {
    return battlesLost;
  }

  public int getHourlyCoins() {
    return hourlyCoins;
  }

  public String getArmyCode() {
    return armyCode;
  }

  public int getNumReferrals() {
    return numReferrals;
  }

  public String getUdid() {
    return udid;
  }

  public Location getUserLocation() {
    return userLocation;
  }

  public int getNumPostsInMarketplace() {
    return numPostsInMarketplace;
  }

  public int getNumMarketplaceSalesUnredeemed() {
    return numMarketplaceSalesUnredeemed;
  }

  public int getWeaponEquipped() {
    return weaponEquipped;
  }

  public int getArmorEquipped() {
    return armorEquipped;
  }

  public int getAmuletEquipped() {
    return amuletEquipped;
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public Date getLastLogout() {
    return lastLogout;
  }

  public String getDeviceToken() {
    return deviceToken;
  }

  public Date getLastBattleNotificationTime() {
    return lastBattleNotificationTime;
  }

  public Date getLastTimeAttacked() {
    return lastTimeAttacked;
  }

  public String getMacAddress() {
    return macAddress;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", name=" + name + ", level=" + level + ", type="
        + type + ", attack=" + attack + ", defense=" + defense + ", stamina="
        + stamina + ", lastStaminaRefillTime=" + lastStaminaRefillTime
        + ", isLastStaminaStateFull=" + isLastStaminaStateFull + ", energy="
        + energy + ", lastEnergyRefillTime=" + lastEnergyRefillTime
        + ", isLastEnergyStateFull=" + isLastEnergyStateFull + ", skillPoints="
        + skillPoints + ", healthMax=" + healthMax + ", energyMax=" + energyMax
        + ", staminaMax=" + staminaMax + ", diamonds=" + diamonds + ", coins="
        + coins + ", marketplaceDiamondsEarnings="
        + marketplaceDiamondsEarnings + ", marketplaceCoinsEarnings="
        + marketplaceCoinsEarnings + ", vaultBalance=" + vaultBalance
        + ", experience=" + experience + ", tasksCompleted=" + tasksCompleted
        + ", battlesWon=" + battlesWon + ", battlesLost=" + battlesLost
        + ", hourlyCoins=" + hourlyCoins + ", armyCode=" + armyCode
        + ", numReferrals=" + numReferrals + ", udid=" + udid
        + ", userLocation=" + userLocation + ", numPostsInMarketplace="
        + numPostsInMarketplace + ", numMarketplaceSalesUnredeemed="
        + numMarketplaceSalesUnredeemed + ", weaponEquipped=" + weaponEquipped
        + ", armorEquipped=" + armorEquipped + ", amuletEquipped="
        + amuletEquipped + ", lastLogin=" + lastLogin + ", lastLogout="
        + lastLogout + ", deviceToken=" + deviceToken
        + ", lastBattleNotificationTime=" + lastBattleNotificationTime
        + ", lastTimeAttacked=" + lastTimeAttacked + ", macAddress="
        + macAddress + "]";
  }


}
