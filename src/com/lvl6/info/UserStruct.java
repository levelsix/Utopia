package com.lvl6.info;

import java.util.Date;

public class UserStruct {
  
  private int userId;
  private int structId;
  private Date lastRetrieved;
  private CoordinatePair coordinates;
  private int level;
  private Date purchaseTime;
  private boolean isComplete;
  
  public UserStruct(int userId, int structId, Date lastRetrieved,
      CoordinatePair coordinates, int level, Date purchaseTime,
      boolean isComplete) {
    this.userId = userId;
    this.structId = structId;
    this.lastRetrieved = lastRetrieved;
    this.coordinates = coordinates;
    this.level = level;
    this.purchaseTime = purchaseTime;
    this.isComplete = isComplete;
  }

  public int getUserId() {
    return userId;
  }

  public int getStructId() {
    return structId;
  }

  public Date getLastRetrieved() {
    return lastRetrieved;
  }

  public CoordinatePair getCoordinates() {
    return coordinates;
  }

  public int getLevel() {
    return level;
  }

  public Date getPurchaseTime() {
    return purchaseTime;
  }

  public boolean isComplete() {
    return isComplete;
  }

  @Override
  public String toString() {
    return "UserStruct [userId=" + userId + ", structId=" + structId
        + ", lastRetrieved=" + lastRetrieved + ", coordinates=" + coordinates
        + ", level=" + level + ", purchaseTime=" + purchaseTime
        + ", isComplete=" + isComplete + "]";
  }  
}
