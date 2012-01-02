package com.lvl6.info;

import java.util.HashMap;
import java.util.Map;

import com.lvl6.properties.DBConstants;
import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;
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
  private int health;
  private int skillPoints;
  private int healthMax;
  private int energyMax;
  private int staminaMax;
  private int diamonds;
  private int coins;
  private int vaultBalance;
  private int experience;
  private int tasksCompleted;
  private int battlesWon;
  private int battlesLost;
  private int hourlyCoins;
  private String armyCode;
  private int armySize;
  private String udid;

  public User(int id, String name, int level, UserType type, int attack,
      int defense, int stamina, int energy, int health, int skillPoints,
      int healthMax, int energyMax, int staminaMax, int diamonds, int coins,
      int vaultBalance, int experience, int tasksCompleted, int battlesWon,
      int battlesLost, int hourlyCoins, String armyCode, int armySize,
      String udid) {
    this.id = id;
    this.name = name;
    this.level = level;
    this.type = type;
    this.attack = attack;
    this.defense = defense;
    this.stamina = stamina;
    this.energy = energy;
    this.health = health;
    this.skillPoints = skillPoints;
    this.healthMax = healthMax;
    this.energyMax = energyMax;
    this.staminaMax = staminaMax;
    this.diamonds = diamonds;
    this.coins = coins;
    this.vaultBalance = vaultBalance;
    this.experience = experience;
    this.tasksCompleted = tasksCompleted;
    this.battlesWon = battlesWon;
    this.battlesLost = battlesLost;
    this.hourlyCoins = hourlyCoins;
    this.armyCode = armyCode;
    this.armySize = armySize;
    this.udid = udid;
  }
  
  /*
   * used for vault
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
  public boolean updateRelativeStaminaExperienceCoinsHealthBattleswonBattleslost (int stamina, int experience, 
      int coins, int health, int battlesWon, int battlesLost) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER__ID, id);
    
    Map <String, Object> relativeParams = new HashMap<String, Object>();
    if (stamina != 0) relativeParams.put(DBConstants.USER__STAMINA, stamina);
    if (experience != 0) relativeParams.put(DBConstants.USER__EXPERIENCE, experience);
    if (coins != 0) relativeParams.put(DBConstants.USER__COINS, coins);
    if (health != 0) relativeParams.put(DBConstants.USER__HEALTH, health);
    if (battlesWon != 0) relativeParams.put(DBConstants.USER__BATTLES_WON, battlesWon);
    if (battlesLost != 0) relativeParams.put(DBConstants.USER__BATTLES_LOST, battlesLost);

    int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER, relativeParams, null, 
        conditionParams, "and");
    if (numUpdated == 1) {
      this.stamina += stamina;
      this.experience += experience;
      this.coins += coins;
      this.health += health;
      this.battlesWon += battlesWon;
      this.battlesLost += battlesLost;
      return true;
    }
    return false;
  }
  
  public boolean incrementUserEquip(int equipId, int increment) {
    Map <String, Object> insertParams = new HashMap<String, Object>();
    insertParams.put(DBConstants.USER_EQUIP__USER_ID, id);
    insertParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);
    insertParams.put(DBConstants.USER_EQUIP__QUANTITY, increment);
    insertParams.put(DBConstants.USER_EQUIP__IS_STOLEN, true);
    int numUpdated = DBConnection.insertOnDuplicateKeyRelativeUpdate(DBConstants.TABLE_USER_EQUIP, insertParams, 
        DBConstants.USER_EQUIP__QUANTITY, increment);
    if (numUpdated == 1) {
      return true;
    }
    return false;
  }
  
  //note: decrement is a positive number
  public boolean decrementUserEquip(int equipId, int currentQuantity, int decrement) {
    Map <String, Object> conditionParams = new HashMap<String, Object>();
    conditionParams.put(DBConstants.USER_EQUIP__USER_ID, id);
    conditionParams.put(DBConstants.USER_EQUIP__EQUIP_ID, equipId);

    if (currentQuantity - decrement < 0) {
      return false;
    }
    if (currentQuantity - decrement <= 0) {
      int numDeleted = DBConnection.deleteRows(DBConstants.TABLE_USER_EQUIP, conditionParams, "and");
      if (numDeleted == 1) {
        return true;
      }
    } else {
      Map <String, Object> relativeParams = new HashMap<String, Object>();
      relativeParams.put(DBConstants.USER_EQUIP__QUANTITY, -1*decrement);      
      int numUpdated = DBConnection.updateTableRows(DBConstants.TABLE_USER_EQUIP, relativeParams, null, 
          conditionParams, "and");
      if (numUpdated == 1) {
        return true;
      }
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

  public int getHealth() {
    return health;
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

  public int getArmySize() {
    return armySize;
  }

  public String getUdid() {
    return udid;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", name=" + name + ", level=" + level + ", type="
        + type + ", attack=" + attack + ", defense=" + defense + ", stamina="
        + stamina + ", energy=" + energy + ", health=" + health
        + ", skillPoints=" + skillPoints + ", healthMax=" + healthMax
        + ", energyMax=" + energyMax + ", staminaMax=" + staminaMax
        + ", diamonds=" + diamonds + ", coins=" + coins + ", vaultBalance="
        + vaultBalance + ", experience=" + experience + ", tasksCompleted="
        + tasksCompleted + ", battlesWon=" + battlesWon + ", battlesLost="
        + battlesLost + ", hourlyCoins=" + hourlyCoins + ", armyCode="
        + armyCode + ", armySize=" + armySize + ", udid=" + udid + "]";
  }
}
