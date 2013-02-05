package com.lvl6.info;

public class ClanTowerUser {
  
  private int battleId;
  private int userId;
  private boolean isInOwnerClan;
  private int pointsGained;
  private int pointsLost;
  
  public ClanTowerUser(int battleId, int userId, int pointsGained, boolean isInOwnerClan,
      int pointsLost) {
    super();
    this.battleId = battleId;
    this.userId = userId;
    this.isInOwnerClan = isInOwnerClan;
    this.pointsGained = pointsGained;
    this.pointsLost = pointsLost;
  }
  public boolean isInOwnerClan() {
    return isInOwnerClan;
  }
  public void setInOwnerClan(boolean isInOwnerClan) {
    this.isInOwnerClan = isInOwnerClan;
  }
  public int getBattleId() {
    return battleId;
  }
  public void setBattleId(int battleId) {
    this.battleId = battleId;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getPointsGained() {
    return pointsGained;
  }
  public void setPointsGained(int pointsGained) {
    this.pointsGained = pointsGained;
  }
  public int getPointsLost() {
    return pointsLost;
  }
  public void setPointsLost(int pointsLost) {
    this.pointsLost = pointsLost;
  }
  @Override
  public String toString() {
    return "ClanTowerUser [battleId=" + battleId + ", userId=" + userId
        + ", isInOwnerClan=" + isInOwnerClan + ", pointsGained=" + pointsGained
        + ", pointsLost=" + pointsLost + "]";
  }
}
