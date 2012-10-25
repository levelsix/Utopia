package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class ClanTower implements Serializable {
  private static final long serialVersionUID = 1936952538114402802L;
  private int id;
  private String towerName;
  private String towerImageName;
  private int clanOwnerId;
  private Date ownedStartTime;
  private int silverReward;
  private int goldReward;
  private int numHoursToCollect;
  private int clanAttackerId;
  private Date attackStartTime;
  private int ownerBattleWins;
  private int attackerBattleWins;
  
  public ClanTower(int id, String towerName, String towerImageName,
      int clanOwnerId, Date ownedStartTime, int silverReward, int goldReward,
      int numHoursToCollect, int clanAttackerId, Date attackStartTime,
      int ownerBattleWins, int attackerBattleWins) {
    super();
    this.id = id;
    this.towerName = towerName;
    this.towerImageName = towerImageName;
    this.clanOwnerId = clanOwnerId;
    this.ownedStartTime = ownedStartTime;
    this.silverReward = silverReward;
    this.goldReward = goldReward;
    this.numHoursToCollect = numHoursToCollect;
    this.clanAttackerId = clanAttackerId;
    this.attackStartTime = attackStartTime;
    this.ownerBattleWins = ownerBattleWins;
    this.attackerBattleWins = attackerBattleWins;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTowerName() {
    return towerName;
  }

  public void setTowerName(String towerName) {
    this.towerName = towerName;
  }

  public String getTowerImageName() {
    return towerImageName;
  }

  public void setTowerImageName(String towerImageName) {
    this.towerImageName = towerImageName;
  }

  public int getClanOwnerId() {
    return clanOwnerId;
  }

  public void setClanOwnerId(int clanOwnerId) {
    this.clanOwnerId = clanOwnerId;
  }

  public Date getOwnedStartTime() {
    return ownedStartTime;
  }

  public void setOwnedStartTime(Date ownedStartTime) {
    this.ownedStartTime = ownedStartTime;
  }

  public int getSilverReward() {
    return silverReward;
  }

  public void setSilverReward(int silverReward) {
    this.silverReward = silverReward;
  }

  public int getGoldReward() {
    return goldReward;
  }

  public void setGoldReward(int goldReward) {
    this.goldReward = goldReward;
  }

  public int getNumHoursToCollect() {
    return numHoursToCollect;
  }

  public void setNumHoursToCollect(int numHoursToCollect) {
    this.numHoursToCollect = numHoursToCollect;
  }

  public int getClanAttackerId() {
    return clanAttackerId;
  }

  public void setClanAttackerId(int clanAttackerId) {
    this.clanAttackerId = clanAttackerId;
  }

  public Date getAttackStartTime() {
    return attackStartTime;
  }

  public void setAttackStartTime(Date attackStartTime) {
    this.attackStartTime = attackStartTime;
  }

  public int getOwnerBattleWins() {
    return ownerBattleWins;
  }

  public void setOwnerBattleWins(int ownerBattleWins) {
    this.ownerBattleWins = ownerBattleWins;
  }

  public int getAttackerBattleWins() {
    return attackerBattleWins;
  }

  public void setAttackerBattleWins(int attackerBattleWins) {
    this.attackerBattleWins = attackerBattleWins;
  }

  @Override
  public String toString() {
    return "ClanTower [id=" + id + ", towerName=" + towerName
        + ", towerImageName=" + towerImageName + ", clanOwnerId=" + clanOwnerId
        + ", ownedStartTime=" + ownedStartTime + ", silverReward="
        + silverReward + ", goldReward=" + goldReward + ", numHoursToCollect="
        + numHoursToCollect + ", clanAttackerId=" + clanAttackerId
        + ", attackStartTime=" + attackStartTime + ", ownerBattleWins="
        + ownerBattleWins + ", attackerBattleWins=" + attackerBattleWins + "]";
  }
}