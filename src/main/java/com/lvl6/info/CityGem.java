package com.lvl6.info;

import java.io.Serializable;

public class CityGem implements Serializable {
  
  private static final long serialVersionUID = 2580610692434091071L;
  private int id;
	private float dropRate;
	private boolean isActive;
  private String gemImageName;
  private boolean droppedOnlyFromBosses;
  
  public CityGem(int id, float dropRate, boolean isActive, String gemImageName,
      boolean droppedOnlyFromBosses) {
    super();
    this.id = id;
    this.dropRate = dropRate;
    this.isActive = isActive;
    this.gemImageName = gemImageName;
    this.droppedOnlyFromBosses = droppedOnlyFromBosses;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public float getDropRate() {
    return dropRate;
  }

  public void setDropRate(float dropRate) {
    this.dropRate = dropRate;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public String getGemImageName() {
    return gemImageName;
  }

  public void setGemImageName(String gemImageName) {
    this.gemImageName = gemImageName;
  }

  public boolean isDroppedOnlyFromBosses() {
    return droppedOnlyFromBosses;
  }

  public void setDroppedOnlyFromBosses(boolean droppedOnlyFromBosses) {
    this.droppedOnlyFromBosses = droppedOnlyFromBosses;
  }

  @Override
  public String toString() {
    return "CityGem [id=" + id + ", dropRate=" + dropRate + ", isActive="
        + isActive + ", gemImageName=" + gemImageName
        + ", droppedOnlyFromBosses=" + droppedOnlyFromBosses + "]";
  }
  
}
