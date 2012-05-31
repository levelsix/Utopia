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
  private int minLevel;
  private int xLength;
  private int yLength;
  private int instaBuildDiamondCost;
  private int instaRetrieveDiamondCostBase;
  private int instaUpgradeDiamondCostBase;
  private int imgVerticalPixelOffset;
  public Structure(int id, String name, int income, int minutesToGain,
      int minutesToBuild, int minutesToUpgradeBase, int coinPrice, int diamondPrice, 
      int minLevel, int xLength, int yLength, int instaBuildDiamondCost, int instaRetrieveDiamondCostBase,
      int instaUpgradeDiamondCostBase, int imgVerticalPixelOffset) {
    this.id = id;
    this.name = name;
    this.income = income;
    this.minutesToGain = minutesToGain;
    this.minutesToBuild = minutesToBuild;
    this.minutesToUpgradeBase = minutesToUpgradeBase;
    this.coinPrice = coinPrice;
    this.diamondPrice = diamondPrice;
    this.minLevel = minLevel;
    this.xLength = xLength;
    this.yLength = yLength;
    this.instaBuildDiamondCost = instaBuildDiamondCost;
    this.instaRetrieveDiamondCostBase = instaRetrieveDiamondCostBase;
    this.instaUpgradeDiamondCostBase = instaUpgradeDiamondCostBase;
    this.imgVerticalPixelOffset = imgVerticalPixelOffset;
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
  public int getMinLevel() {
    return minLevel;
  }
  public int getxLength() {
    return xLength;
  }
  public int getyLength() {
    return yLength;
  }
  public int getInstaBuildDiamondCost() {
    return instaBuildDiamondCost;
  }
  public int getInstaRetrieveDiamondCostBase() {
    return instaRetrieveDiamondCostBase;
  }
  public int getInstaUpgradeDiamondCostBase() {
    return instaUpgradeDiamondCostBase;
  }
  public int getImgVerticalPixelOffset() {
    return imgVerticalPixelOffset;
  }
  
  @Override
  public String toString() {
    return "Structure [id=" + id + ", name=" + name + ", income=" + income
        + ", minutesToGain=" + minutesToGain + ", minutesToBuild="
        + minutesToBuild + ", minutesToUpgradeBase=" + minutesToUpgradeBase
        + ", coinPrice=" + coinPrice + ", diamondPrice=" + diamondPrice
        + ", minLevel=" + minLevel + ", xLength=" + xLength + ", yLength="
        + yLength + ", instaBuildDiamondCost=" + instaBuildDiamondCost
        + ", instaRetrieveDiamondCostBase=" + instaRetrieveDiamondCostBase
        + ", instaUpgradeDiamondCostBase=" + instaUpgradeDiamondCostBase
        + ", xOffset=" + imgVerticalPixelOffset + "]";
  }
  
}