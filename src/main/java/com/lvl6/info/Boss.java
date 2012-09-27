package com.lvl6.info;

import java.io.Serializable;

public class Boss implements Serializable {
  private static final long serialVersionUID = 4045679768497161961L;
  private int id;
  private String goodName;
  private String badName;
  private int cityId;
  private int assetNumberWithinCity;
  private int staminaCost;
  private int minDamage;
  private int maxDamage;
  private int minutesToKill;
  private int minutesToRespawn;
  private int baseHealth;
  private int expGained;

  public Boss(int id, String goodName, String badName, int cityId,
      int assetNumberWithinCity, int staminaCost, int minDamage, int maxDamage,
      int minutesToKill, int minutesToRespawn, int baseHealth, int expGained) {
    super();
    this.id = id;
    this.goodName = goodName;
    this.badName = badName;
    this.cityId = cityId;
    this.assetNumberWithinCity = assetNumberWithinCity;
    this.staminaCost = staminaCost;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.minutesToKill = minutesToKill;
    this.minutesToRespawn = minutesToRespawn;
    this.baseHealth = baseHealth;
    this.expGained = expGained;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getGoodName() {
    return goodName;
  }
  public void setGoodName(String goodName) {
    this.goodName = goodName;
  }
  public String getBadName() {
    return badName;
  }
  public void setBadName(String badName) {
    this.badName = badName;
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
  
  public int getExpGained() {
    return expGained;
  }
  
  public void setExpGained(int expGained) {
    this.expGained = expGained;
  }
  
  public int getMinutesToKill() {
    return minutesToKill;
  }

  public void setMinutesToKill(int minutesToKill) {
    this.minutesToKill = minutesToKill;
  }

  public int getMinutesToRespawn() {
    return minutesToRespawn;
  }

  public void setMinutesToRespawn(int minutesToRespawn) {
    this.minutesToRespawn = minutesToRespawn;
  }

  public int getBaseHealth() {
    return baseHealth;
  }

  public void setBaseHealth(int baseHealth) {
    this.baseHealth = baseHealth;
  }

  @Override
  public String toString() {
    return "Boss [id=" + id + ", goodName=" + goodName + ", badName=" + badName
        + ", cityId=" + cityId + ", assetNumberWithinCity="
        + assetNumberWithinCity + ", staminaCost=" + staminaCost
        + ", minDamage=" + minDamage + ", maxDamage=" + maxDamage
        + ", minutesToKill=" + minutesToKill + ", minutesToRespawn="
        + minutesToRespawn + ", baseHealth=" + baseHealth + ", expGained="
        + expGained + "]";
  }

}
