package com.lvl6.info;

import java.io.Serializable;

public class BoosterPack implements Serializable {
  private static final long serialVersionUID = -5134245989644009715L;
  private int id;
  private boolean costsCoins;
  private int salePrice;
  private int retailPrice;
  private String name;
  private String chestImage;
  private String middleImage;
  private String backgroundImage;
  private int minLevel;
  private int maxLevel;
  
  public BoosterPack(int id, boolean costsCoins, int salePrice, int retailPrice, String name, 
      String chestImage, String middleImage, String backgroundImage, int minLevel, int maxLevel) {
    super();
    this.id = id;
    this.costsCoins = costsCoins;
    this.salePrice = salePrice;
    this.retailPrice = retailPrice;
    this.name = name;
    this.chestImage = chestImage;
    this.middleImage = middleImage;
    this.backgroundImage = backgroundImage;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isCostsCoins() {
    return costsCoins;
  }

  public void setCostsCoins(boolean costsCoins) {
    this.costsCoins = costsCoins;
  }

  public int getSalePrice() {
    return salePrice;
  }

  public void setSalePrice(int salePrice) {
    this.salePrice = salePrice;
  }

  public int getRetailPrice() {
    return retailPrice;
  }

  public void setRetailPrice(int retailPrice) {
    this.retailPrice = retailPrice;
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

  public String getBackgroundImage() {
    return backgroundImage;
  }

  public void setBackgroundImage(String backgroundImage) {
    this.backgroundImage = backgroundImage;
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
    return "BoosterPack [id=" + id + ", costsCoins=" + costsCoins
        + ", salePrice=" + salePrice + ", retailPrice=" + retailPrice
        + ", name=" + name + ", chestImage=" + chestImage + ", middleImage="
        + middleImage + ", backgroundImage=" + backgroundImage + ", minLevel="
        + minLevel + ", maxLevel=" + maxLevel + "]";
  }

}
