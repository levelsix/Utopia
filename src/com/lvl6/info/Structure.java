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
  private int upgradeCoinCost;
  private int upgradeDiamondCost;
  
  public Structure(int id, String name, int income, int minutesToGain,
      int coinPrice, int diamondPrice, int minLevel, int minArmy, int xLength,
      int yLength, int upgradeCoinCost, int upgradeDiamondCost) {
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
    this.upgradeCoinCost = upgradeCoinCost;
    this.upgradeDiamondCost = upgradeDiamondCost;
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
  public int getIncome() {
    return income;
  }
  public void setIncome(int income) {
    this.income = income;
  }
  public int getMinutesToGain() {
    return minutesToGain;
  }
  public void setMinutesToGain(int minutesToGain) {
    this.minutesToGain = minutesToGain;
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
  public int getMinLevel() {
    return minLevel;
  }
  public void setMinLevel(int minLevel) {
    this.minLevel = minLevel;
  }
  public int getMinArmy() {
    return minArmy;
  }
  public void setMinArmy(int minArmy) {
    this.minArmy = minArmy;
  }
  public int getxLength() {
    return xLength;
  }
  public void setxLength(int xLength) {
    this.xLength = xLength;
  }
  public int getyLength() {
    return yLength;
  }
  public void setyLength(int yLength) {
    this.yLength = yLength;
  }
  public int getUpgradeCoinCost() {
    return upgradeCoinCost;
  }
  public void setUpgradeCoinCost(int upgradeCoinCost) {
    this.upgradeCoinCost = upgradeCoinCost;
  }
  public int getUpgradeDiamondCost() {
    return upgradeDiamondCost;
  }
  public void setUpgradeDiamondCost(int upgradeDiamondCost) {
    this.upgradeDiamondCost = upgradeDiamondCost;
  }

  @Override
  public String toString() {
    return "Structure [id=" + id + ", name=" + name + ", income=" + income
        + ", minutesToGain=" + minutesToGain + ", coinPrice=" + coinPrice
        + ", diamondPrice=" + diamondPrice + ", minLevel=" + minLevel
        + ", minArmy=" + minArmy + ", xLength=" + xLength + ", yLength="
        + yLength + ", upgradeCoinCost=" + upgradeCoinCost
        + ", upgradeDiamondCost=" + upgradeDiamondCost + "]";
  }
}
