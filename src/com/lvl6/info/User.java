package com.lvl6.info;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto.StatType;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
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
  private int energy;
  private Date lastEnergyRefillTime;
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
  private int flees;
  private String referralCode;
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
  private int numBadges;
  private Date lastShortLicensePurchaseTime;
  private Date lastLongLicensePurchaseTime;
  private boolean isFake;

  public User(int id, String name, int level, UserType type, int attack,
      int defense, int stamina, Date lastStaminaRefillTime,
      int energy, Date lastEnergyRefillTime,
      int skillPoints, int healthMax,
      int energyMax, int staminaMax, int diamonds, int coins,
      int marketplaceDiamondsEarnings, int marketplaceCoinsEarnings,
      int vaultBalance, int experience, int tasksCompleted, int battlesWon,
      int battlesLost, int flees, String referralCode, int numReferrals,
      String udid, Location userLocation, int numPostsInMarketplace,
      int numMarketplaceSalesUnredeemed, int weaponEquipped, int armorEquipped,
      int amuletEquipped, Date lastLogin, Date lastLogout, String deviceToken,
      Date lastBattleNotificationTime, Date lastTimeAttacked,
      int numBadges, Date lastShortLicensePurchaseTime,
      Date lastLongLicensePurchaseTime, boolean isFake) {
    this.id = id;
    this.name = name;
    this.level = level;
    this.type = type;
    this.attack = attack;
    this.defense = defense;
    this.stamina = stamina;
    this.lastStaminaRefillTime = lastStaminaRefillTime;
    this.energy = energy;
    this.lastEnergyRefillTime = lastEnergyRefillTime;
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
    this.flees = flees;
    this.referralCode = referralCode;
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
    this.numBadges = numBadges;
    this.lastShortLicensePurchaseTime = lastShortLicensePurchaseTime;
    this.lastLongLicensePurchaseTime = lastLongLicensePurchaseTime;
    this.isFake = isFake;
  }

  public boolean updateAbsoluteUserLocation(Location location) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LATITUDE, location.getLatitude());
    absoluteParams.put(DBConstants.USER__LONGITUDE, location.getLongitude());

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.userLocation = location;
      return true;
    }
    return false;
  }
  
  public boolean updateEquipped(Equipment equipment) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);
    
    int equipId = equipment.getId();

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (equipment.getType() == EquipType.WEAPON) {
      absoluteParams.put(DBConstants.USER__WEAPON_EQUIPPED, equipId);
    }
    if (equipment.getType() == EquipType.ARMOR) {
      absoluteParams.put(DBConstants.USER__ARMOR_EQUIPPED, equipId);      
    }
    if (equipment.getType() == EquipType.AMULET) {
      absoluteParams.put(DBConstants.USER__AMULET_EQUIPPED, equipId);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (equipment.getType() == EquipType.WEAPON) {
        this.weaponEquipped = equipId;
      }
      if (equipment.getType() == EquipType.ARMOR) {
        this.armorEquipped = equipId;
      }
      if (equipment.getType() == EquipType.AMULET) {
        this.amuletEquipped = equipId;
      }
      return true;
    }
    return false;
  }

  public boolean updateUnequip(int equipId, boolean isWeapon, boolean isArmor, boolean isAmulet) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (isWeapon) {
      absoluteParams.put(DBConstants.USER__WEAPON_EQUIPPED, null);
    }
    if (isArmor) {
      absoluteParams.put(DBConstants.USER__ARMOR_EQUIPPED, null);      
    }
    if (isAmulet) {
      absoluteParams.put(DBConstants.USER__AMULET_EQUIPPED, null);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (isWeapon) {
        this.weaponEquipped = ControllerConstants.NOT_SET;
      }
      if (isArmor) {
        this.armorEquipped = ControllerConstants.NOT_SET;
      }
      if (isAmulet) {
        this.amuletEquipped = ControllerConstants.NOT_SET;
      }
      return true;
    }
    return false;
  }

  public boolean updateRelativeDiamondsAbsoluteLastshortlicensepurchasetimeLastlonglicensepurchasetime(int diamondChange, 
      Timestamp lastShortLicensePurchaseTime, Timestamp lastLongLicensePurchaseTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (lastShortLicensePurchaseTime != null) {
      absoluteParams.put(DBConstants.USER__LAST_SHORT_LICENSE_PURCHASE_TIME, lastShortLicensePurchaseTime);
    }
    if (lastLongLicensePurchaseTime != null) {
      absoluteParams.put(DBConstants.USER__LAST_LONG_LICENSE_PURCHASE_TIME, lastLongLicensePurchaseTime);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += diamondChange;
      if (lastShortLicensePurchaseTime != null) {
        this.lastShortLicensePurchaseTime = lastShortLicensePurchaseTime;
      }
      if (lastLongLicensePurchaseTime != null) {
        this.lastLongLicensePurchaseTime = lastLongLicensePurchaseTime;
      }
      return true;
    }
    return false;
  }

  public boolean updateSetdevicetoken(String deviceToken) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__DEVICE_TOKEN, deviceToken);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.deviceToken = deviceToken;
      return true;
    }
    return false;
  }

  public boolean updateResetNumbadgesSetdevicetoken(String deviceToken) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__NUM_BADGES, 0);
    absoluteParams.put(DBConstants.USER__DEVICE_TOKEN, deviceToken);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, null, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.numBadges = 0;
      this.deviceToken = deviceToken;
      return true;
    }
    return false;
  }

  public boolean updateRelativeBadge(int badgeChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__NUM_BADGES, badgeChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.numBadges += badgeChange;
      return true;
    }
    return false;
  }

  public boolean updateRelativeBadgeAbsoluteLastbattlenotificationtime(int badgeChange, Timestamp newLastBattleNotificationTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LAST_BATTLE_NOTIFICATION_TIME, newLastBattleNotificationTime);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__NUM_BADGES, badgeChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.numBadges += badgeChange;
      this.lastBattleNotificationTime = newLastBattleNotificationTime;
      return true;
    }
    return false;
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
  public boolean updateLaststaminarefilltimeStaminaIslaststaminastatefull(Timestamp lastStaminaRefillTime, int staminaChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, lastStaminaRefillTime);

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
      return true;
    }
    return false;
  }

  /*
   * used for refilling energy
   */
  public boolean updateLastenergyrefilltimeEnergy(Timestamp lastEnergyRefillTime, int energyChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, lastEnergyRefillTime);

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
      return true;
    }
    return false;
  }

  /*
   * used for using diamonds to refill stat
   */
  public boolean updateRelativeDiamondsRestoreStat (int diamondChange, StatType statType) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (statType == StatType.ENERGY) {
      absoluteParams.put(DBConstants.USER__ENERGY, energyMax);
    } else if (statType == StatType.STAMINA) {
      absoluteParams.put(DBConstants.USER__STAMINA, staminaMax);
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, 
        absoluteParams, conditionParams, "and");
    if (numUpdated == 1) {
      if (statType == StatType.ENERGY) {
        this.energy = energyMax;
      } else if (statType == StatType.STAMINA) {
        this.stamina = staminaMax;
      }
      this.diamonds += diamondChange;
      return true;
    }
    return false;
  }

  /*
   * used for leveling up
   */
  public boolean updateAbsoluteRestoreEnergyStaminaRelativeUpdateSkillPoints(int skillPointsChange, Timestamp levelUpTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = null;
    if (skillPointsChange > 0) {
      relativeParams = new HashMap<String, Object>();
      relativeParams.put(DBConstants.USER__SKILL_POINTS, skillPointsChange);
    }
    
    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    absoluteParams.put(DBConstants.USER__ENERGY, energyMax);
    absoluteParams.put(DBConstants.USER__STAMINA, staminaMax);
    absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, levelUpTime);
    absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, levelUpTime);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.energy = energyMax;
      this.stamina = staminaMax;
      this.skillPoints += skillPointsChange;
      this.lastEnergyRefillTime = levelUpTime;
      this.lastStaminaRefillTime = levelUpTime;
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
      absoluteParams.put(DBConstants.USER__LAST_ENERGY_REFILL_TIME, clientTime);
    }
    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (simulateEnergyRefill) {
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

  public boolean updateRelativeDiamondsNumreferrals (int diamondChange, int numReferralsChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    if (diamondChange != 0) {
      relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    }
    if (numReferralsChange != 0) {
      relativeParams.put(DBConstants.USER__NUM_REFERRALS, numReferralsChange); 
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += diamondChange;
      this.numReferrals += numReferralsChange;
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
  public boolean updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill (int stamina, int experience, 
      int coins, int battlesWon, int battlesLost, int fleesChange,  boolean simulateStaminaRefill, boolean updateLastAttackedTime, Timestamp clientTime) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    if (stamina != 0) relativeParams.put(DBConstants.USER__STAMINA, stamina);
    if (experience != 0) relativeParams.put(DBConstants.USER__EXPERIENCE, experience);
    if (coins != 0) relativeParams.put(DBConstants.USER__COINS, coins);
    if (battlesWon != 0) relativeParams.put(DBConstants.USER__BATTLES_WON, battlesWon);
    if (battlesLost != 0) relativeParams.put(DBConstants.USER__BATTLES_LOST, battlesLost);
    if (fleesChange != 0) relativeParams.put(DBConstants.USER__FLEES, fleesChange);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();
    if (simulateStaminaRefill) {
      absoluteParams.put(DBConstants.USER__LAST_STAMINA_REFILL_TIME, clientTime);
    }
    if (updateLastAttackedTime) {
      absoluteParams.put(DBConstants.USER__LAST_TIME_ATTACKED, clientTime);
    }
    if (absoluteParams.size() == 0) {
      absoluteParams = null;
    }

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      if (simulateStaminaRefill) {
        this.lastStaminaRefillTime = clientTime;
      }
      if (updateLastAttackedTime) {
        this.lastTimeAttacked = clientTime;
      }
      this.stamina += stamina;
      this.experience += experience;
      this.coins += coins;
      this.battlesWon += battlesWon;
      this.battlesLost += battlesLost;
      this.flees += fleesChange;
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

  public int getEnergy() {
    return energy;
  }

  public Date getLastEnergyRefillTime() {
    return lastEnergyRefillTime;
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

  public int getFlees() {
    return flees;
  }

  public String getReferralCode() {
    return referralCode;
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

  public int getNumBadges() {
    return numBadges;
  }

  public Date getLastShortLicensePurchaseTime() {
    return lastShortLicensePurchaseTime;
  }

  public Date getLastLongLicensePurchaseTime() {
    return lastLongLicensePurchaseTime;
  }

  public boolean isFake() {
    return isFake;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", name=" + name + ", level=" + level + ", type="
        + type + ", attack=" + attack + ", defense=" + defense + ", stamina="
        + stamina + ", lastStaminaRefillTime=" + lastStaminaRefillTime
        + ", energy=" + energy + ", lastEnergyRefillTime="
        + lastEnergyRefillTime + ", skillPoints=" + skillPoints
        + ", healthMax=" + healthMax + ", energyMax=" + energyMax
        + ", staminaMax=" + staminaMax + ", diamonds=" + diamonds + ", coins="
        + coins + ", marketplaceDiamondsEarnings="
        + marketplaceDiamondsEarnings + ", marketplaceCoinsEarnings="
        + marketplaceCoinsEarnings + ", vaultBalance=" + vaultBalance
        + ", experience=" + experience + ", tasksCompleted=" + tasksCompleted
        + ", battlesWon=" + battlesWon + ", battlesLost=" + battlesLost
        + ", flees=" + flees + ", referralCode=" + referralCode
        + ", numReferrals=" + numReferrals + ", udid=" + udid
        + ", userLocation=" + userLocation + ", numPostsInMarketplace="
        + numPostsInMarketplace + ", numMarketplaceSalesUnredeemed="
        + numMarketplaceSalesUnredeemed + ", weaponEquipped=" + weaponEquipped
        + ", armorEquipped=" + armorEquipped + ", amuletEquipped="
        + amuletEquipped + ", lastLogin=" + lastLogin + ", lastLogout="
        + lastLogout + ", deviceToken=" + deviceToken
        + ", lastBattleNotificationTime=" + lastBattleNotificationTime
        + ", lastTimeAttacked=" + lastTimeAttacked + ", numBadges=" + numBadges
        + ", lastShortLicensePurchaseTime=" + lastShortLicensePurchaseTime
        + ", lastLongLicensePurchaseTime=" + lastLongLicensePurchaseTime
        + ", isFake=" + isFake + "]";
  }

}
