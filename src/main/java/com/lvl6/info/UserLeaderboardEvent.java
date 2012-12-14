package com.lvl6.info;

import java.io.Serializable;

public class UserLeaderboardEvent implements Serializable {
  private static final long serialVersionUID = -4146319195339195482L;
  private int leaderboardEventId;
  private int userId;
  private int battlesWon;
  private int battlesLost;
  private int battlesFled;
  
  public UserLeaderboardEvent(int leaderboardEventId, int userId, int battlesWon,
      int battlesLost, int battlesFled) {
    super();
    this.leaderboardEventId = leaderboardEventId;
    this.userId = userId;
    this.battlesWon = battlesWon;
    this.battlesLost = battlesLost;
    this.battlesFled = battlesFled;
  }
  public int getLeaderboardEventId() {
    return leaderboardEventId;
  }
  public void setLeaderboardEventId(int leaderboardEventId) {
    this.leaderboardEventId = leaderboardEventId;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getBattlesWon() {
    return battlesWon;
  }
  public void setBattlesWon(int battlesWon) {
    this.battlesWon = battlesWon;
  }
  public int getBattlesLost() {
    return battlesLost;
  }
  public void setBattlesLost(int battlesLost) {
    this.battlesLost = battlesLost;
  }
  public int getBattlesFled() {
    return battlesFled;
  }
  public void setBattlesFled(int battlesFled) {
    this.battlesFled = battlesFled;
  }
  @Override
  public String toString() {
    return "BossEvent [leaderboardEventId=" + leaderboardEventId + ", userId="
        + userId + ", battlesWon=" + battlesWon + ", battlesLost=" + battlesLost
        + ", battlesFled=" + battlesFled + "]";
  }
}