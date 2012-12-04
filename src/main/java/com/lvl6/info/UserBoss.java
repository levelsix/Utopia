package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class UserBoss implements Serializable {
  private static final long serialVersionUID = -6613940156097697826L;
  private int userId;
  private int bossId;
  private int currentHealth;
  private int numTimesKilled;
  private Date startTime;
  private Date lastTimeKilled;

  public UserBoss(int userId, int bossId, int currentHealth, int numTimesKilled, Date startTime,
      Date lastTimeKilled) {
    super();
    this.userId = userId;
    this.bossId = bossId;
    this.currentHealth = currentHealth;
    this.numTimesKilled = numTimesKilled;
    this.startTime = startTime;
    this.lastTimeKilled = lastTimeKilled;
  }
  
  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
  
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getBossId() {
    return bossId;
  }
  public void setBossId(int bossId) {
    this.bossId = bossId;
  }
  public int getCurrentHealth() {
    return currentHealth;
  }
  public void setCurrentHealth(int currentHealth) {
    this.currentHealth = currentHealth;
  }
  public int getNumTimesKilled() {
    return numTimesKilled;
  }
  public void setNumTimesKilled(int numTimesKilled) {
    this.numTimesKilled = numTimesKilled;
  }
  public Date getLastTimeKilled() {
    return lastTimeKilled;
  }
  public void setLastTimeKilled(Date lastTimeKilled) {
    this.lastTimeKilled = lastTimeKilled;
  }
  @Override
  public String toString() {
    return "UserBoss [userId=" + userId + ", bossId=" + bossId
        + ", currentHealth=" + currentHealth + ", numTimesKilled="
        + numTimesKilled + ", startTime=" + startTime 
        + "lastTimeKilled" + lastTimeKilled+ "]";
  }
}
