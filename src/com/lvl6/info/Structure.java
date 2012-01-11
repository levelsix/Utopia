package com.lvl6.info;

public class Structure {
  
  private int id;
  private String name;
  private int income;
  private int minutesToGain;
  private int coinPrice;
  private int diamondPrice;
  private int minLevel;
  private int xLength;
  private int yLength;
  private int upgradeCoinCost;
  private int upgradeDiamondCost;
  
  public Structure(int id, String name, int income, int minutesToGain,
      int coinPrice, int diamondPrice, int minLevel, int xLength,
      int yLength, int upgradeCoinCost, int upgradeDiamondCost) {
    this.id = id;
    this.name = name;
    this.income = income;
    this.minutesToGain = minutesToGain;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.minLevel = minLevel;
    this.xLength = xLength;
    this.yLength = yLength;
    this.upgradeCoinCost = upgradeCoinCost;
    this.upgradeDiamondCost = upgradeDiamondCost;
  }
  
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getIncome() {
    return income;
  }

  public int getMinutesToGain() {
    return minutesToGain;
  }

  public int getCoinPrice() {
    return coinPrice;
  }

  public int getDiamondPrice() {
    return diamondPrice;
  }

  public int getMinLevel() {
    return minLevel;
  }

  public int getxLength() {
    return xLength;
  }

  public int getyLength() {
    return yLength;
  }

  public int getUpgradeCoinCost() {
    return upgradeCoinCost;
  }

  public int getUpgradeDiamondCost() {
    return upgradeDiamondCost;
  }

  @Override
  public String toString() {
    return "Structure [id=" + id + ", name=" + name + ", income=" + income
        + ", minutesToGain=" + minutesToGain + ", coinPrice=" + coinPrice
        + ", diamondPrice=" + diamondPrice + ", minLevel=" + minLevel
        + ", xLength=" + xLength + ", yLength="
        + yLength + ", upgradeCoinCost=" + upgradeCoinCost
        + ", upgradeDiamondCost=" + upgradeDiamondCost + "]";
  }
}
