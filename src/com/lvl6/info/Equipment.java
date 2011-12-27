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



  public void setId(int id) {
    this.id = id;
  }



  public String getName() {
    return name;
  }



  public void setName(String name) {
    this.name = name;
  }



  public EquipType getType() {
    return type;
  }



  public void setType(EquipType type) {
    this.type = type;
  }



  public int getAttackBoost() {
    return attackBoost;
  }



  public void setAttackBoost(int attackBoost) {
    this.attackBoost = attackBoost;
  }



  public int getDefenseBoost() {
    return defenseBoost;
  }



  public void setDefenseBoost(int defenseBoost) {
    this.defenseBoost = defenseBoost;
  }



  public int getMinLevel() {
    return minLevel;
  }



  public void setMinLevel(int minLevel) {
    this.minLevel = minLevel;
  }



  public int getCoinPrice() {
    return coinPrice;
  }



  public void setCoinPrice(int coinPrice) {
    this.coinPrice = coinPrice;
  }



  public int getDiamondPrice() {
    return diamondPrice;
  }



  public void setDiamondPrice(int diamondPrice) {
    this.diamondPrice = diamondPrice;
  }



  public enum EquipType {
    WEAPON(0),
    MELEE(1),
    ARMOR(2),
    AMULET(3),;

    public static final int WEAPON_VALUE = 0;
    public static final int MELEE_VALUE = 1;
    public static final int ARMOR_VALUE = 2;
    public static final int AMULET_VALUE = 3;

    public final int getNumber() { return value; }

    public static EquipType valueOf(int value) {
      switch (value) {
      case 0: return WEAPON;
      case 1: return MELEE;
      case 2: return ARMOR;
      case 3: return AMULET;
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
