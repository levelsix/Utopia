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
  private Date endTimeWithoutSpeedup;
  private int coinCost;
  private int diamondGuaranteeCost;
  private Date timeOfSpeedup;
  private boolean attemptComplete;
  
  public BlacksmithAttempt(int id, int userId, int equipId,
      int goalLevel, boolean guaranteed, Date startTime,
      Date endTimeWithoutSpeedup, int coinCost, int diamondGuaranteeCost,
      Date timeOfSpeedup, boolean attemptComplete) {
    this.id = id;
    this.userId = userId;
    this.equipId = equipId;
    this.goalLevel = goalLevel;
    this.guaranteed = guaranteed;
    this.startTime = startTime;
    this.endTimeWithoutSpeedup = endTimeWithoutSpeedup;
    this.coinCost = coinCost;
    this.diamondGuaranteeCost = diamondGuaranteeCost;
    this.timeOfSpeedup = timeOfSpeedup;
    this.attemptComplete = attemptComplete;
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

  public Date getEndTimeWithoutSpeedup() {
    return endTimeWithoutSpeedup;
  }

  public int getCoinCost() {
    return coinCost;
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

  @Override
  public String toString() {
    return "UnhandledBlacksmithAttempt [id=" + id + ", userId=" + userId
        + ", equipId=" + equipId + ", goalLevel=" + goalLevel + ", guaranteed="
        + guaranteed + ", startTime=" + startTime + ", endTimeWithoutSpeedup="
        + endTimeWithoutSpeedup + ", coinCost=" + coinCost
        + ", diamondGuaranteeCost=" + diamondGuaranteeCost + ", timeOfSpeedup="
        + timeOfSpeedup + ", attemptComplete=" + attemptComplete + "]";
  }
  
}
