package com.lvl6.info;

public class Structure {
  
  private int id;
  private String name;
  private int income;
  private int minutesToGain;
  private int coinPrice;
  private int diamondPrice;
  private int minLevel;
  private int minArmy;
  private int xLength;
  private int yLength;
  
  public Structure(int id, String name, int income, int minutesToGain,
      int coinPrice, int diamondPrice, int minLevel, int minArmy, int xLength,
      int yLength) {
    this.id = id;
    this.name = name;
    this.income = income;
    this.minutesToGain = minutesToGain;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.minLevel = minLevel;
    this.minArmy = minArmy;
    this.xLength = xLength;
    this.yLength = yLength;
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

  public int getMinArmy() {
    return minArmy;
  }

  public int getxLength() {
    return xLength;
  }

  public int getyLength() {
    return yLength;
  }

  @Override
  public String toString() {
    return "Structure [id=" + id + ", name=" + name + ", income=" + income
        + ", minutesToGain=" + minutesToGain + ", coinPrice=" + coinPrice
        + ", diamondPrice=" + diamondPrice + ", minLevel=" + minLevel
        + ", minArmy=" + minArmy + ", xLength=" + xLength + ", yLength="
        + yLength + "]";
  }
}
