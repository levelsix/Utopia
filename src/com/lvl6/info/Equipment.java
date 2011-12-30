package com.lvl6.info;

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

  public Equipment(int id, String name, EquipType type, int attackBoost,
      int defenseBoost, int minLevel, int coinPrice, int diamondPrice) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.attackBoost = attackBoost;
    this.defenseBoost = defenseBoost;
    this.minLevel = minLevel;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
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

  public enum EquipType {
    WEAPON(0),
    ARMOR(1),
    AMULET(2),;

    public final int getNumber() { return value; }

    public static EquipType valueOf(int value) {
      switch (value) {
      case 0: return WEAPON;
      case 1: return ARMOR;
      case 2: return AMULET;
      default: return null;
      }
    }

    private final int value;

    private EquipType(int value) {
      this.value = value;
    }
  }

  @Override
  public String toString() {
    return "Equipment [id=" + id + ", name=" + name + ", type=" + type
        + ", attackBoost=" + attackBoost + ", defenseBoost=" + defenseBoost
        + ", minLevel=" + minLevel + ", coinPrice=" + coinPrice
        + ", diamondPrice=" + diamondPrice + "]";
  }

}
