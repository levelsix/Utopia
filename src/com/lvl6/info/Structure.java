package com.lvl6.info;

public class Structure {
    
  private int id;
  private String name;
  private int income;
  private int minutesToGain;
  private int minutesToBuild;
  private int minutesToUpgradeBase;
  private int coinPrice;
  private int diamondPrice;
  private int woodPrice;
  private int minLevel;
  private int xLength;
  private int yLength;
  private int upgradeCoinCostBase;
  private int upgradeDiamondCostBase;
  private int upgradeWoodCostBase;
  private int instaBuildDiamondCostBase;
  private int instaRetrieveDiamondCostBase;
  private int instaUpgradeDiamondCostBase;
  public Structure(int id, String name, int income, int minutesToGain,
      int minutesToBuild, int minutesToUpgradeBase, int coinPrice, int diamondPrice, int woodPrice,
      int minLevel, int xLength, int yLength, int upgradeCoinCostBase,
      int upgradeDiamondCostBase, int upgradeWoodCostBase,
      int instaBuildDiamondCostBase, int instaRetrieveDiamondCostBase,
      int instaUpgradeDiamondCostBase) {
    this.id = id;
    this.name = name;
    this.income = income;
    this.minutesToGain = minutesToGain;
    this.minutesToBuild = minutesToBuild;
    this.minutesToUpgradeBase = minutesToUpgradeBase;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.woodPrice = woodPrice;
    this.minLevel = minLevel;
    this.xLength = xLength;
    this.yLength = yLength;
    this.upgradeCoinCostBase = upgradeCoinCostBase;
    this.upgradeDiamondCostBase = upgradeDiamondCostBase;
    this.upgradeWoodCostBase = upgradeWoodCostBase;
    this.instaBuildDiamondCostBase = instaBuildDiamondCostBase;
    this.instaRetrieveDiamondCostBase = instaRetrieveDiamondCostBase;
    this.instaUpgradeDiamondCostBase = instaUpgradeDiamondCostBase;
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
  public int getMinutesToBuild() {
    return minutesToBuild;
  }
  public int getMinutesToUpgradeBase() {
    return minutesToUpgradeBase;
  }
  public int getCoinPrice() {
    return coinPrice;
  }
  public int getDiamondPrice() {
    return diamondPrice;
  }
  public int getWoodPrice() {
    return woodPrice;
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
  public int getUpgradeCoinCostBase() {
    return upgradeCoinCostBase;
  }
  public int getUpgradeDiamondCostBase() {
    return upgradeDiamondCostBase;
  }
  public int getUpgradeWoodCostBase() {
    return upgradeWoodCostBase;
  }
  public int getInstaBuildDiamondCostBase() {
    return instaBuildDiamondCostBase;
  }
  public int getInstaRetrieveDiamondCostBase() {
    return instaRetrieveDiamondCostBase;
  }
  public int getInstaUpgradeDiamondCostBase() {
    return instaUpgradeDiamondCostBase;
  }
  @Override
  public String toString() {
    return "Structure [id=" + id + ", name=" + name + ", income=" + income
        + ", minutesToGain=" + minutesToGain + ", minutesToBuild="
        + minutesToBuild + ", minutesToUpgradeBase=" + minutesToUpgradeBase
        + ", coinPrice=" + coinPrice + ", diamondPrice=" + diamondPrice
        + ", woodPrice=" + woodPrice + ", minLevel=" + minLevel + ", xLength="
        + xLength + ", yLength=" + yLength + ", upgradeCoinCostBase="
        + upgradeCoinCostBase + ", upgradeDiamondCostBase="
        + upgradeDiamondCostBase + ", upgradeWoodCostBase="
        + upgradeWoodCostBase + ", instaBuildDiamondCostBase="
        + instaBuildDiamondCostBase + ", instaRetrieveDiamondCostBase="
        + instaRetrieveDiamondCostBase + ", instaUpgradeDiamondCostBase="
        + instaUpgradeDiamondCostBase + "]";
  }
  
}
