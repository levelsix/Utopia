package com.lvl6.info;

import java.io.Serializable;

public class BoosterPack implements Serializable {
  private static final long serialVersionUID = -5134245989644009715L;
  private int id;
  private int coinCost;
  private int diamondCost;
  private String name;
  private String chestImage;
  private String middleImage;
  private int minLevel;
  private int maxLevel;
  
  public BoosterPack(int id, int coinCost, int diamondCost, String name, 
      String chestImage, String middleImage, int minLevel, int maxLevel) {
    super();
    this.id = id;
    this.coinCost = coinCost;
    this.diamondCost = diamondCost;
    this.name = name;
    this.chestImage = chestImage;
    this.middleImage = middleImage;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getCoinCost() {
    return coinCost;
  }

  public void setCoinCost(int coinCost) {
    this.coinCost = coinCost;
  }

  public int getDiamondCost() {
    return diamondCost;
  }

  public void setDiamondCost(int diamondCost) {
    this.diamondCost = diamondCost;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getChestImage() {
    return chestImage;
  }

  public void setChestImage(String chestImage) {
    this.chestImage = chestImage;
  }

  public String getMiddleImage() {
    return middleImage;
  }

  public void setMiddleImage(String middleImage) {
    this.middleImage = middleImage;
  }

  public int getMinLevel() {
    return minLevel;
  }

  public void setMinLevel(int minLevel) {
    this.minLevel = minLevel;
  }

  public int getMaxLevel() {
    return maxLevel;
  }

  public void setMaxLevel(int maxLevel) {
    this.maxLevel = maxLevel;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public String toString() {
    return "BoosterPack [id=" + id + ", coinCost=" + coinCost
        + ", diamondCost=" + diamondCost + ", name=" + name + ", chestImage="
        + chestImage + ", middleImage=" + middleImage + ", minLevel="
        + minLevel + ", maxLevel=" + maxLevel + "]";
  }

}
