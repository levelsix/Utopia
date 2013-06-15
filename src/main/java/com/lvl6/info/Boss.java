package com.lvl6.info;

import java.io.Serializable;

public class Boss implements Serializable {
  private static final long serialVersionUID = 4045679768497161961L;
  private int id;
  private int cityId;
  private int assetNumberWithinCity;
  private int regularAttackEnergyCost;
  private int minDamage;
  private int maxDamage;
  private int minutesToKill;
  private int baseHealth;
  private int baseExp;
  private float superAttackDamageMultiplier;
  private int superAttackEnergyCost;
  private String goodName;
  private String badName;
  
  
  public Boss(int id, int cityId, int assetNumberWithinCity,
      int regularAttackEnergyCost, int minDamage, int maxDamage,
      int minutesToKill, int baseHealth, int baseExp,
      float superAttackDamageMultiplier, int superAttackEnergyCost,
      String goodName, String badName) {
    super();
    this.id = id;
    this.cityId = cityId;
    this.assetNumberWithinCity = assetNumberWithinCity;
    this.regularAttackEnergyCost = regularAttackEnergyCost;
    this.minDamage = minDamage;
    this.maxDamage = maxDamage;
    this.minutesToKill = minutesToKill;
    this.baseHealth = baseHealth;
    this.baseExp = baseExp;
    this.superAttackDamageMultiplier = superAttackDamageMultiplier;
    this.superAttackEnergyCost = superAttackEnergyCost;
    this.goodName = goodName;
    this.badName = badName;
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


  public int getRegularAttackEnergyCost() {
    return regularAttackEnergyCost;
  }


  public void setRegularAttackEnergyCost(int regularAttackEnergyCost) {
    this.regularAttackEnergyCost = regularAttackEnergyCost;
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


  public int getBaseExp() {
    return baseExp;
  }


  public void setBaseExp(int baseExp) {
    this.baseExp = baseExp;
  }


  public float getSuperAttackDamageMultiplier() {
    return superAttackDamageMultiplier;
  }


  public void setSuperAttackDamageMultiplier(float superAttackDamageMultiplier) {
    this.superAttackDamageMultiplier = superAttackDamageMultiplier;
  }


  public int getSuperAttackEnergyCost() {
    return superAttackEnergyCost;
  }


  public void setSuperAttackEnergyCost(int superAttackEnergyCost) {
    this.superAttackEnergyCost = superAttackEnergyCost;
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


  @Override
  public String toString() {
    return "Boss [id=" + id + ", cityId=" + cityId + ", assetNumberWithinCity="
        + assetNumberWithinCity + ", regularAttackEnergyCost="
        + regularAttackEnergyCost + ", minDamage=" + minDamage + ", maxDamage="
        + maxDamage + ", minutesToKill=" + minutesToKill + ", baseHealth="
        + baseHealth + ", baseExp=" + baseExp
        + ", superAttackDamageMultiplier=" + superAttackDamageMultiplier
        + ", superAttackEnergyCost=" + superAttackEnergyCost + ", goodName="
        + goodName + ", badName=" + badName + "]";
  }
  
}
