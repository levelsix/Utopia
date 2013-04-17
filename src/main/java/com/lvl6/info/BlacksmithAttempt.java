package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class BlacksmithAttempt implements Serializable {
  
  private static final long serialVersionUID = 4280332245815139564L;
  private int id;
  private int userId;
  private int equipId;
  private int goalLevel;
  private boolean guaranteed;
  private Date startTime;
  private int diamondGuaranteeCost;
  private Date timeOfSpeedup;
  private boolean attemptComplete;
  private int equipOneEnhancementPercent;
  private int equipTwoEnhancementPercent;
  private int forgeSlotNumber;

  public BlacksmithAttempt(int id, int userId, int equipId, int goalLevel,
      boolean guaranteed, Date startTime, int diamondGuaranteeCost, 
      Date timeOfSpeedup, boolean attemptComplete,
      int equipOneEnhancementPercent, int equipTwoEnhancementPercent,
      int forgeSlotNumber) {
    this.id = id;
    this.userId = userId;
    this.equipId = equipId;
    this.goalLevel = goalLevel;
    this.guaranteed = guaranteed;
    this.startTime = startTime;
    this.diamondGuaranteeCost = diamondGuaranteeCost;
    this.timeOfSpeedup = timeOfSpeedup;
    this.attemptComplete = attemptComplete;
    this.equipOneEnhancementPercent = equipOneEnhancementPercent;
    this.equipTwoEnhancementPercent = equipTwoEnhancementPercent;
    this.forgeSlotNumber = forgeSlotNumber;
  }

  public int getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public int getEquipId() {
    return equipId;
  }

  public int getGoalLevel() {
    return goalLevel;
  }

  public boolean isGuaranteed() {
    return guaranteed;
  }

  public Date getStartTime() {
    return startTime;
  }

  public int getDiamondGuaranteeCost() {
    return diamondGuaranteeCost;
  }

  public Date getTimeOfSpeedup() {
    return timeOfSpeedup;
  }

  public boolean isAttemptComplete() {
    return attemptComplete;
  }

  public int getEquipOneEnhancementPercent() {
    return equipOneEnhancementPercent;
  }

  public void setEquipOneEnhancementPercent(int equipOneEnhancementPercent) {
    this.equipOneEnhancementPercent = equipOneEnhancementPercent;
  }

  public int getEquipTwoEnhancementPercent() {
    return equipTwoEnhancementPercent;
  }

  public void setEquipTwoEnhancementPercent(int equipTwoEnhancementPercent) {
    this.equipTwoEnhancementPercent = equipTwoEnhancementPercent;
  }

  public int getForgeSlotNumber() {
    return forgeSlotNumber;
  }

  public void setForgeSlotNumber(int forgeSlotNumber) {
    this.forgeSlotNumber = forgeSlotNumber;
  }

  @Override
  public String toString() {
    return "BlacksmithAttempt [id=" + id + ", userId=" + userId + ", equipId="
        + equipId + ", goalLevel=" + goalLevel + ", guaranteed=" + guaranteed
        + ", startTime=" + startTime + ", diamondGuaranteeCost="
        + diamondGuaranteeCost + ", timeOfSpeedup=" + timeOfSpeedup
        + ", attemptComplete=" + attemptComplete
        + ", equipOneEnhancementPercent=" + equipOneEnhancementPercent
        + ", equipTwoEnhancementPercent=" + equipTwoEnhancementPercent
        + ", forgeSlotNumber=" + forgeSlotNumber + "]";
  }

}
