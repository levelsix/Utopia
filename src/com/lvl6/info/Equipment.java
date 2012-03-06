package com.lvl6.info;

import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;

public class Equipment {
  public static final int NOT_SET = -1;

  private int id;
  private String name;
  private EquipType type;
  private String description;
  private int attackBoost;
  private int defenseBoost;
  private int minLevel;
  private int coinPrice = NOT_SET;
  private int diamondPrice = NOT_SET;
  private float chanceOfLoss = NOT_SET;
  private ClassType classType;
  private Rarity rarity;
  private boolean isBuyableInArmory;

  public Equipment(int id, String name, EquipType type, String description, int attackBoost,
      int defenseBoost, int minLevel, int coinPrice, int diamondPrice, float chanceOfLoss, 
      ClassType classType, Rarity rarity, boolean isBuyableInArmory) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.description = description;
    this.attackBoost = attackBoost;
    this.defenseBoost = defenseBoost;
    this.minLevel = minLevel;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.chanceOfLoss = chanceOfLoss;
    this.classType = classType;
    this.rarity = rarity;
    this.isBuyableInArmory = isBuyableInArmory;
  }

  public static int getNotSet() {
    return NOT_SET;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public EquipType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public int getAttackBoost() {
    return attackBoost;
  }

  public int getDefenseBoost() {
    return defenseBoost;
  }

  public int getMinLevel() {
    return minLevel;
  }

  public int getCoinPrice() {
    return coinPrice;
  }

  public int getDiamondPrice() {
    return diamondPrice;
  }

  public float getChanceOfLoss() {
    return chanceOfLoss;
  }

  public ClassType getClassType() {
    return classType;
  }

  public Rarity getRarity() {
    return rarity;
  }

  public boolean isBuyableInArmory() {
    return isBuyableInArmory;
  }

  @Override
  public String toString() {
    return "Equipment [id=" + id + ", name=" + name + ", type=" + type
        + ", description=" + description + ", attackBoost=" + attackBoost
        + ", defenseBoost=" + defenseBoost + ", minLevel=" + minLevel
        + ", coinPrice=" + coinPrice + ", diamondPrice=" + diamondPrice
        + ", chanceOfLoss=" + chanceOfLoss + ", classType=" + classType
        + ", rarity=" + rarity + ", isBuyableInArmory=" + isBuyableInArmory
        + "]";
  }

}
