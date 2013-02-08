package com.lvl6.info;

import java.io.Serializable;

public class BoosterPack implements Serializable {
  private static final long serialVersionUID = -5134245989644009715L;
  private int id;
  private boolean costsCoins;
  private String name;
  private String chestImage;
  private String middleImage;
  private String backgroundImage;
  private int minLevel;
  private int maxLevel;
  private int salePriceOne;
  private int retailPriceOne;
  private int salePriceTwo;
  private int retailPriceTwo;
  
  public BoosterPack(int id, boolean costsCoins, String name, String chestImage, String middleImage,
      String backgroundImage, int minLevel, int maxLevel, int salePriceOne, int retailPriceOne,
      int salePriceTwo, int retailPriceTwo) {
    super();
    this.id = id;
    this.costsCoins = costsCoins;
    this.name = name;
    this.chestImage = chestImage;
    this.middleImage = middleImage;
    this.backgroundImage = backgroundImage;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.salePriceOne = salePriceOne;
    this.retailPriceOne = retailPriceOne;
    this.salePriceTwo = salePriceTwo;
    this.retailPriceTwo = retailPriceTwo;
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

  public int getSalePriceOne() {
    return salePriceOne;
  }

  public void setSalePriceOne(int salePriceOne) {
    this.salePriceOne = salePriceOne;
  }

  public int getRetailPriceOne() {
    return retailPriceOne;
  }

  public void setRetailPriceOne(int retailPriceOne) {
    this.retailPriceOne = retailPriceOne;
  }

  public int getSalePriceTwo() {
    return salePriceTwo;
  }

  public void setSalePriceTwo(int salePriceTwo) {
    this.salePriceTwo = salePriceTwo;
  }

  public int getRetailPriceTwo() {
    return retailPriceTwo;
  }

  public void setRetailPriceTwo(int retailPriceTwo) {
    this.retailPriceTwo = retailPriceTwo;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public String toString() {
    return "BoosterPack [id=" + id + ", costsCoins=" + costsCoins + ", name="
        + name + ", chestImage=" + chestImage + ", middleImage=" + middleImage
        + ", backgroundImage=" + backgroundImage + ", minLevel=" + minLevel
        + ", maxLevel=" + maxLevel + ", salePriceOne=" + salePriceOne
        + ", retailPriceOne=" + retailPriceOne + ", salePriceTwo="
        + salePriceTwo + ", retailPriceTwo=" + retailPriceTwo + "]";
  }

}
