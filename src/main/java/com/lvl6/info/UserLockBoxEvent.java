package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class UserLockBoxEvent implements Serializable {
  private static final long serialVersionUID = -2707386084442793864L;
  private int lockBoxId;
  private int userId;
  private int numLockBoxes;
  private int numTimesCompleted;
  private Date lastPickTime;
  
  public UserLockBoxEvent(int lockBoxId, int userId, int numLockBoxes,
      int numTimesCompleted, Date lastPickTime) {
    super();
    this.lockBoxId = lockBoxId;
    this.userId = userId;
    this.numLockBoxes = numLockBoxes;
    this.numTimesCompleted = numTimesCompleted;
    this.lastPickTime = lastPickTime;
  }

  public int getLockBoxId() {
    return lockBoxId;
  }

  public void setLockBoxId(int lockBoxId) {
    this.lockBoxId = lockBoxId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getNumLockBoxes() {
    return numLockBoxes;
  }

  public void setNumLockBoxes(int numLockBoxes) {
    this.numLockBoxes = numLockBoxes;
  }

  public int getNumTimesCompleted() {
    return numTimesCompleted;
  }

  public void setNumTimesCompleted(int numTimesCompleted) {
    this.numTimesCompleted = numTimesCompleted;
  }

  public Date getLastPickTime() {
    return lastPickTime;
  }

  public void setLastPickTime(Date lastPickTime) {
    this.lastPickTime = lastPickTime;
  }

  @Override
  public String toString() {
    return "UserLockBoxEvent [lockBoxId=" + lockBoxId + ", userId=" + userId
        + ", numLockBoxes=" + numLockBoxes + ", numTimesCompleted="
        + numTimesCompleted + ", lastPickTime=" + lastPickTime + "]";
  }
  
}
