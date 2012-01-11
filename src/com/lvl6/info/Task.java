package com.lvl6.info;

import java.util.List;

public class Task {

  private int id;
  private String goodName;
  private String badName;
  private int cityId;
  private int energyCost;
  private int minCoinsGained;
  private int maxCoinsGained;
  private float chanceOfEquipFloat;
  private List<Integer> potentialLootEquipIds;
  private int expGained;
  private int assetNumberWithinCity;
  private int numForCompletion;
  
  public Task(int id, String goodName, String badName, int cityId, int energyCost, int minCoinsGained,
      int maxCoinsGained, float chanceOfEquipFloat,
      List<Integer> potentialLootEquipIds, int expGained,
      int assetNumberWithinCity, int numForCompletion) {
    this.id = id;
    this.goodName = goodName;
    this.badName = badName;
    this.cityId = cityId;
    this.energyCost = energyCost;
    this.minCoinsGained = minCoinsGained;
    this.maxCoinsGained = maxCoinsGained;
    this.chanceOfEquipFloat = chanceOfEquipFloat;
    this.potentialLootEquipIds = potentialLootEquipIds;
    this.expGained = expGained;
    this.assetNumberWithinCity = assetNumberWithinCity;
    this.numForCompletion = numForCompletion;
  }

  public int getId() {
    return id;
  }

  public String getGoodName() {
    return goodName;
  }

  public String getBadName() {
    return badName;
  }

  public int getCityId() {
    return cityId;
  }

  public int getEnergyCost() {
    return energyCost;
  }

  public int getMinCoinsGained() {
    return minCoinsGained;
  }

  public int getMaxCoinsGained() {
    return maxCoinsGained;
  }

  public float getChanceOfEquipFloat() {
    return chanceOfEquipFloat;
  }

  public List<Integer> getPotentialLootEquipIds() {
    return potentialLootEquipIds;
  }

  public int getExpGained() {
    return expGained;
  }

  public int getAssetNumberWithinCity() {
    return assetNumberWithinCity;
  }

  public int getNumForCompletion() {
    return numForCompletion;
  }

  @Override
  public String toString() {
    return "Task [id=" + id + ", goodName=" + goodName + ", badName=" + badName
        + ", cityId=" + cityId + ", energyCost=" + energyCost + ", minCoinsGained="
        + minCoinsGained + ", maxCoinsGained=" + maxCoinsGained
        + ", chanceOfEquipFloat=" + chanceOfEquipFloat
        + ", potentialLootEquipIds=" + potentialLootEquipIds + ", expGained="
        + expGained + ", assetNumberWithinCity=" + assetNumberWithinCity
        + ", numForCompletion=" + numForCompletion + "]";
  }

}
