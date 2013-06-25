package com.lvl6.info;

import java.io.Serializable;

public class Boss implements Serializable {
  private static final long serialVersionUID = 4045679768497161961L;
  private int id;
  private int cityId;
  private int assetNumberWithinCity;
  private int regularAttackEnergyCost;
  private int minutesToKill;
  private float superAttackDamageMultiplier;
  private int superAttackEnergyCost;
  private String name;
  private int expConstantA;
  private int expConstantB;
  private int hpConstantA;
  private int hpConstantB;
  private int hpConstantC;
  private int dmgConstantA;
  private int dmgConstantB;
  private String mapImageName;
  
  public Boss(int id, int cityId, int assetNumberWithinCity,
      int regularAttackEnergyCost, int minutesToKill,
      float superAttackDamageMultiplier, int superAttackEnergyCost,
      String name, int expConstantA, int expConstantB, int hpConstantA,
      int hpConstantB, int hpConstantC, int dmgConstantA, int dmgConstantB,
      String mapImageName) {
    super();
    this.id = id;
    this.cityId = cityId;
    this.assetNumberWithinCity = assetNumberWithinCity;
    this.regularAttackEnergyCost = regularAttackEnergyCost;
    this.minutesToKill = minutesToKill;
    this.superAttackDamageMultiplier = superAttackDamageMultiplier;
    this.superAttackEnergyCost = superAttackEnergyCost;
    this.name = name;
    this.expConstantA = expConstantA;
    this.expConstantB = expConstantB;
    this.hpConstantA = hpConstantA;
    this.hpConstantB = hpConstantB;
    this.hpConstantC = hpConstantC;
    this.dmgConstantA = dmgConstantA;
    this.dmgConstantB = dmgConstantB;
    this.mapImageName = mapImageName;
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

  public int getMinutesToKill() {
    return minutesToKill;
  }

  public void setMinutesToKill(int minutesToKill) {
    this.minutesToKill = minutesToKill;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getExpConstantA() {
    return expConstantA;
  }

  public void setExpConstantA(int expConstantA) {
    this.expConstantA = expConstantA;
  }

  public int getExpConstantB() {
    return expConstantB;
  }

  public void setExpConstantB(int expConstantB) {
    this.expConstantB = expConstantB;
  }

  public int getHpConstantA() {
    return hpConstantA;
  }

  public void setHpConstantA(int hpConstantA) {
    this.hpConstantA = hpConstantA;
  }

  public int getHpConstantB() {
    return hpConstantB;
  }

  public void setHpConstantB(int hpConstantB) {
    this.hpConstantB = hpConstantB;
  }

  public int getHpConstantC() {
    return hpConstantC;
  }

  public void setHpConstantC(int hpConstantC) {
    this.hpConstantC = hpConstantC;
  }

  public int getDmgConstantA() {
    return dmgConstantA;
  }

  public void setDmgConstantA(int dmgConstantA) {
    this.dmgConstantA = dmgConstantA;
  }

  public int getDmgConstantB() {
    return dmgConstantB;
  }

  public void setDmgConstantB(int dmgConstantB) {
    this.dmgConstantB = dmgConstantB;
  }

  public String getMapImageName() {
    return mapImageName;
  }

  public void setMapImageName(String mapImageName) {
    this.mapImageName = mapImageName;
  }

  @Override
  public String toString() {
    return "Boss [id=" + id + ", cityId=" + cityId + ", assetNumberWithinCity="
        + assetNumberWithinCity + ", regularAttackEnergyCost="
        + regularAttackEnergyCost + ", minutesToKill=" + minutesToKill
        + ", superAttackDamageMultiplier=" + superAttackDamageMultiplier
        + ", superAttackEnergyCost=" + superAttackEnergyCost + ", name=" + name
        + ", expConstantA=" + expConstantA + ", expConstantB=" + expConstantB
        + ", hpConstantA=" + hpConstantA + ", hpConstantB=" + hpConstantB
        + ", hpConstantC=" + hpConstantC + ", dmgConstantA=" + dmgConstantA
        + ", dmgConstantB=" + dmgConstantB + ", mapImageName=" + mapImageName
        + "]";
  }
  
}
