package com.lvl6.info;

import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;

public class Equipment {
  public static final int NOT_SET = -1;

  private int id;
  private String name;
  private EquipType type;
  private int attackBoost;
  private int defenseBoost;
  private int minLevel;
  private int coinPrice = NOT_SET;
  private int diamondPrice = NOT_SET;
  private float chanceOfLoss = NOT_SET;

  public Equipment(int id, String name, EquipType type, int attackBoost,
      int defenseBoost, int minLevel, int coinPrice, int diamondPrice, float chanceOfLoss) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.attackBoost = attackBoost;
    this.defenseBoost = defenseBoost;
    this.minLevel = minLevel;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.chanceOfLoss = chanceOfLoss;
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

  @Override
  public String toString() {
    return "Equipment [id=" + id + ", name=" + name + ", type=" + type
        + ", attackBoost=" + attackBoost + ", defenseBoost=" + defenseBoost
        + ", minLevel=" + minLevel + ", coinPrice=" + coinPrice
        + ", diamondPrice=" + diamondPrice + "]";
  }

}
