package com.lvl6.info;

import java.io.Serializable;

public class Boss implements Serializable {
  private static final long serialVersionUID = 4045679768497161961L;
  private int id;
  private int cityId;
  private int assetNumberWithinCity;
  private int staminaCost;
  private int minDamage;
  private int maxDamage;
  private int minutesToKill;
  private int baseHealth;
  private int minExp;
  private int maxExp;

  public Boss(int id, int cityId,
      int assetNumberWithinCity, int staminaCost, int minDamage, int maxDamage,
      int minutesToKill, int baseHealth, int minExp, int maxExp) {
    super();
    this.id = id;
    this.cityId = cityId;
    this.assetNumberWithinCity = assetNumberWithinCity;
    this.staminaCost = staminaCost;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.minutesToKill = minutesToKill;
    this.baseHealth = baseHealth;
    this.minExp = minExp;
    this.maxExp = maxExp;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public int getCityId() {
    return cityId;
  }
  public void setCityId(int cityId) {
    this.cityId = cityId;
  }
  public int getAssetNumberWithinCity() {
    return assetNumberWithinCity;
  }
  public void setAssetNumberWithinCity(int assetNumberWithinCity) {
    this.assetNumberWithinCity = assetNumberWithinCity;
  }
  public int getStaminaCost() {
    return staminaCost;
  }
  public void setStaminaCost(int staminaCost) {
    this.staminaCost = staminaCost;
  }
  
  public int getMinDamage() {
    return minDamage;
  }
  
  public void setMinDamage(int minDamage) {
    this.minDamage = minDamage;
  }
  
  public int getMaxDamage() {
    return maxDamage;
  }
  
  public void setMaxDamage(int maxDamage) {
    this.maxDamage = maxDamage;
  }
  
  public int getMinExp() {
    return minExp;
  }
  
  public void setMinExp(int minExp) {
    this.minExp = minExp;
  }
  
  public int getMaxExp() {
    return maxExp;
  }
  
  public void setMaxExp(int maxExp) {
    this.maxExp = maxExp;
  }
  
  public int getMinutesToKill() {
    return minutesToKill;
  }

  public void setMinutesToKill(int minutesToKill) {
    this.minutesToKill = minutesToKill;
  }

  public int getBaseHealth() {
    return baseHealth;
  }

  public void setBaseHealth(int baseHealth) {
    this.baseHealth = baseHealth;
  }

  @Override
  public String toString() {
    return "Boss [id=" + id + ", cityId=" + cityId + ", assetNumberWithinCity="
        + assetNumberWithinCity + ", staminaCost=" + staminaCost
        + ", minDamage=" + minDamage + ", maxDamage=" + maxDamage
        + ", minutesToKill=" + minutesToKill + ", baseHealth=" + baseHealth
        + ", minExp=" + minExp + ", maxExp=" + maxExp + "]";
  }

}
