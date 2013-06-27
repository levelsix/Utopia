package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class UserBoss implements Serializable {
  private static final long serialVersionUID = -6613940156097697826L;
  private int bossId;
  private int userId;
  private int currentHealth;
  private int currentLevel; //starts at 0
  private Date startTime;
  private int gemlessStreak;
  
  public UserBoss(int bossId, int userId, int currentHealth, int currentLevel,
      Date startTime, int gemlessStreak) {
    super();
    this.bossId = bossId;
    this.userId = userId;
    this.currentHealth = currentHealth;
    this.currentLevel = currentLevel;
    this.startTime = startTime;
    this.gemlessStreak = gemlessStreak;
  }

  public int getBossId() {
    return bossId;
  }

  public void setBossId(int bossId) {
    this.bossId = bossId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getCurrentHealth() {
    return currentHealth;
  }

  public void setCurrentHealth(int currentHealth) {
    this.currentHealth = currentHealth;
  }

  public int getCurrentLevel() {
    return currentLevel;
  }

  public void setCurrentLevel(int currentLevel) {
    this.currentLevel = currentLevel;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public int getGemlessStreak() {
    return gemlessStreak;
  }

  public void setGemlessStreak(int gemlessStreak) {
    this.gemlessStreak = gemlessStreak;
  }

  @Override
  public String toString() {
    return "UserBoss [bossId=" + bossId + ", userId=" + userId
        + ", currentHealth=" + currentHealth + ", currentLevel=" + currentLevel
        + ", startTime=" + startTime + ", gemlessStreak=" + gemlessStreak + "]";
  }

}
