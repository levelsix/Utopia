package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

import com.lvl6.properties.ControllerConstants;


public class MonteCard implements Serializable {
  private static final long serialVersionUID = -980514205850473476L;
  private int id;
  private int diamondGained = ControllerConstants.NOT_SET;
  private int coinsGained = ControllerConstants.NOT_SET;
  private int warriorEquipId = ControllerConstants.NOT_SET;
  private int warriorEquipLevel = ControllerConstants.NOT_SET;
  private int archerEquipId = ControllerConstants.NOT_SET;
  private int archerEquipLevel = ControllerConstants.NOT_SET;
  private int mageEquipId = ControllerConstants.NOT_SET;
  private int mageEquipLevel = ControllerConstants.NOT_SET;
  private Date startDate;
  private MonteCardType cardType;

  public MonteCard(int id, int diamondCost, int coinCost, int warriorEquipId,
      int warriorEquipLevel, int archerEquipId, int archerEquipLevel,
      int mageEquipId, int mageEquipLevel, Date startDate,
      MonteCardType cardType) {
    super();
    this.id = id;
    this.diamondGained = diamondCost;
    this.coinsGained = coinCost;
    this.warriorEquipId = warriorEquipId;
    this.warriorEquipLevel = warriorEquipLevel;
    this.archerEquipId = archerEquipId;
    this.archerEquipLevel = archerEquipLevel;
    this.mageEquipId = mageEquipId;
    this.mageEquipLevel = mageEquipLevel;
    this.startDate = startDate;
    this.cardType = cardType;
  }
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getDiamondsGained() {
    return diamondGained;
  }

  public void setDiamondsGained(int diamondsGained) {
    this.diamondGained = diamondsGained;
  }

  public int getCoinsGained() {
    return coinsGained;
  }

  public void setCoinsGained(int coinsGained) {
    this.coinsGained = coinsGained;
  }

  public int getWarriorEquipId() {
    return warriorEquipId;
  }

  public void setWarriorEquipId(int warriorEquipId) {
    this.warriorEquipId = warriorEquipId;
  }

  public int getWarriorEquipLevel() {
    return warriorEquipLevel;
  }

  public void setWarriorEquipLevel(int warriorEquipLevel) {
    this.warriorEquipLevel = warriorEquipLevel;
  }

  public int getArcherEquipId() {
    return archerEquipId;
  }

  public void setArcherEquipId(int archerEquipId) {
    this.archerEquipId = archerEquipId;
  }

  public int getArcherEquipLevel() {
    return archerEquipLevel;
  }

  public void setArcherEquipLevel(int archerEquipLevel) {
    this.archerEquipLevel = archerEquipLevel;
  }

  public int getMageEquipId() {
    return mageEquipId;
  }

  public void setMageEquipId(int mageEquipId) {
    this.mageEquipId = mageEquipId;
  }

  public int getMageEquipLevel() {
    return mageEquipLevel;
  }

  public void setMageEquipLevel(int mageEquipLevel) {
    this.mageEquipLevel = mageEquipLevel;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public MonteCardType getCardType() {
    return cardType;
  }

  public void setCardType(MonteCardType cardType) {
    this.cardType = cardType;
  }

  public enum MonteCardType {
    BAD(0), MEDIUM(1), GOOD(2);
    private int value;
    
    private MonteCardType(int c) {
      value = c;
    }
    
    public static MonteCardType valueOf(int c) {
      switch (c) {
      case 0: return BAD;
      case 1: return MEDIUM;
      case 2: return GOOD;
      default: return null;
    }
    }
    
    public int getValue() {
      return value;
    }
  }
}