package com.lvl6.info;

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
  private int energy;
  private int skillPoints;
  private int healthMax;
  private int energyMax;
  private int staminaMax;
  private int diamonds;
  private int coins;
  private int wood;
  private int marketplaceDiamondsEarnings;
  private int marketplaceCoinsEarnings;
  private int marketplaceWoodEarnings;
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

  public User(int id, String name, int level, UserType type, int attack,
      int defense, int stamina, int energy, int skillPoints,
      int healthMax, int energyMax, int staminaMax, int diamonds, int coins,
      int wood, int marketplaceDiamondsEarnings, int marketplaceCoinsEarnings,
      int marketplaceWoodEarnings, int vaultBalance, int experience,
      int tasksCompleted, int battlesWon, int battlesLost, int hourlyCoins,
      String armyCode, int numReferrals, String udid, Location userLocation,
      int numPostsInMarketplace, int numMarketplaceSalesUnredeemed) {
    this.id = id;
    this.name = name;
    this.level = level;
    this.type = type;
    this.attack = attack;
    this.defense = defense;
    this.stamina = stamina;
    this.energy = energy;
    this.skillPoints = skillPoints;
    this.healthMax = healthMax;
    this.energyMax = energyMax;
    this.staminaMax = staminaMax;
    this.diamonds = diamonds;
    this.coins = coins;
    this.wood = wood;
    this.marketplaceDiamondsEarnings = marketplaceDiamondsEarnings;
    this.marketplaceCoinsEarnings = marketplaceCoinsEarnings;
    this.marketplaceWoodEarnings = marketplaceWoodEarnings;
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
  public boolean updateRelativeDiamondsCoinsWoodExperienceNaive (int diamondChange, int coinChange, 
      int woodChange, int experienceChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__WOOD, woodChange);
    relativeParams.put(DBConstants.USER__EXPERIENCE, experienceChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += diamondChange;
      this.coins += coinChange;
      this.wood += woodChange;
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
    relativeParams.put(DBConstants.USER__WOOD, marketplaceWoodEarnings);

    Map <String, Object> absoluteParams = new HashMap<String, Object>();

    absoluteParams.put(DBConstants.USER__MARKETPLACE_DIAMONDS_EARNINGS, 0);
    absoluteParams.put(DBConstants.USER__MARKETPLACE_COINS_EARNINGS, 0);
    absoluteParams.put(DBConstants.USER__MARKETPLACE_WOOD_EARNINGS, 0);
    absoluteParams.put(DBConstants.USER__NUM_MARKETPLACE_SALES_UNREDEEMED, 0);
    
    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, absoluteParams, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.diamonds += marketplaceDiamondsEarnings;
      this.coins += marketplaceCoinsEarnings;
      this.wood += marketplaceWoodEarnings;
      this.marketplaceDiamondsEarnings = 0;
      this.marketplaceCoinsEarnings = 0;
      this.marketplaceWoodEarnings = 0;
      return true;
    }
    return false;
  }
  
  /*
   * used for marketplace purchase
   */
  public boolean updateRelativeDiamondsearningsCoinsearningsWoodearningsNumpostsinmarketplaceNummarketplacesalesunredeemedNaive 
  (int diamondEarningsChange, int coinEarningsChange, 
      int woodEarningsChange, int numPostsInMarketplaceChange, int numMarketplaceSalesUnredeemedChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__MARKETPLACE_DIAMONDS_EARNINGS, diamondEarningsChange);
    relativeParams.put(DBConstants.USER__MARKETPLACE_COINS_EARNINGS, coinEarningsChange);
    relativeParams.put(DBConstants.USER__MARKETPLACE_WOOD_EARNINGS, woodEarningsChange);
    relativeParams.put(DBConstants.USER__NUM_POSTS_IN_MARKETPLACE, numPostsInMarketplaceChange);
    relativeParams.put(DBConstants.USER__NUM_MARKETPLACE_SALES_UNREDEEMED, numMarketplaceSalesUnredeemedChange);
    
    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.marketplaceDiamondsEarnings += diamondEarningsChange;
      this.marketplaceCoinsEarnings += coinEarningsChange;
      this.marketplaceWoodEarnings += woodEarningsChange;
      this.numPostsInMarketplace += numPostsInMarketplaceChange;
      this.numMarketplaceSalesUnredeemed += numMarketplaceSalesUnredeemedChange;
      return true;
    }
    return false;
  }


  /*
   * used for marketplace
   */
  public boolean updateRelativeDiamondsCoinsWoodNumpostsinmarketplaceNaive (int diamondChange, int coinChange, 
      int woodChange, int numPostsInMarketplaceChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__DIAMONDS, diamondChange);
    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__WOOD, woodChange);
    relativeParams.put(DBConstants.USER__NUM_POSTS_IN_MARKETPLACE, numPostsInMarketplaceChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.coins += coinChange;
      this.diamonds += diamondChange;
      this.wood += woodChange;
      this.numPostsInMarketplace += numPostsInMarketplaceChange;
      return true;
    }
    return false;
  }


  /*
   * used for tasks
   *        * user- coins/exp/tasks_completed increase, energy decrease
   */
  public boolean updateRelativeCoinsExpTaskscompletedEnergy (int coinChange, int expChange, int tasksCompletedChange, 
      int energyChange) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();

    relativeParams.put(DBConstants.USER__COINS, coinChange);
    relativeParams.put(DBConstants.USER__EXPERIENCE, expChange);
    relativeParams.put(DBConstants.USER__TASKS_COMPLETED, tasksCompletedChange);
    relativeParams.put(DBConstants.USER__ENERGY, energyChange);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
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
  public boolean updateRelativeStaminaExperienceCoinsBattleswonBattleslost (int stamina, int experience, 
      int coins, int battlesWon, int battlesLost) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);

    Map <String, Object> relativeParams = new HashMap<String, Object>();
    if (stamina != 0) relativeParams.put(DBConstants.USER__STAMINA, stamina);
    if (experience != 0) relativeParams.put(DBConstants.USER__EXPERIENCE, experience);
    if (coins != 0) relativeParams.put(DBConstants.USER__COINS, coins);
    if (battlesWon != 0) relativeParams.put(DBConstants.USER__BATTLES_WON, battlesWon);
    if (battlesLost != 0) relativeParams.put(DBConstants.USER__BATTLES_LOST, battlesLost);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
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

  public int getEnergy() {
    return energy;
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

  public int getWood() {
    return wood;
  }

  public int getMarketplaceDiamondsEarnings() {
    return marketplaceDiamondsEarnings;
  }

  public int getMarketplaceCoinsEarnings() {
    return marketplaceCoinsEarnings;
  }

  public int getMarketplaceWoodEarnings() {
    return marketplaceWoodEarnings;
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

  @Override
  public String toString() {
    return "User [id=" + id + ", name=" + name + ", level=" + level + ", type="
        + type + ", attack=" + attack + ", defense=" + defense + ", stamina="
        + stamina + ", energy=" + energy + ", skillPoints=" + skillPoints
        + ", healthMax=" + healthMax + ", energyMax=" + energyMax
        + ", staminaMax=" + staminaMax + ", diamonds=" + diamonds + ", coins="
        + coins + ", wood=" + wood + ", marketplaceDiamondsEarnings="
        + marketplaceDiamondsEarnings + ", marketplaceCoinsEarnings="
        + marketplaceCoinsEarnings + ", marketplaceWoodEarnings="
        + marketplaceWoodEarnings + ", vaultBalance=" + vaultBalance
        + ", experience=" + experience + ", tasksCompleted=" + tasksCompleted
        + ", battlesWon=" + battlesWon + ", battlesLost=" + battlesLost
        + ", hourlyCoins=" + hourlyCoins + ", armyCode=" + armyCode
        + ", numReferrals=" + numReferrals + ", udid=" + udid
        + ", userLocation=" + userLocation + ", numPostsInMarketplace="
        + numPostsInMarketplace + ", numMarketplaceSalesUnredeemed="
        + numMarketplaceSalesUnredeemed + "]";
  }
}
