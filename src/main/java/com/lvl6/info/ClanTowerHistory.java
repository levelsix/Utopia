package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class ClanTowerHistory implements Serializable {
  private static final long serialVersionUID = 1936952538114402802L;
  private int clanOwnerId;
  private int clanAttackerId;
  private int towerId;
  private Date attackStartTime;
  private int winnerId;
  private int ownerBattleWins;
  private int attackerBattleWins;
  private int numHoursForBattle;
  private Date lastRewardGiven;
  private Date timeOfEntry;
  private String reasonForEntry;

  public ClanTowerHistory(int clanOwnerId, int clanAttackerId, int towerId,
      Date attackStartTime, int winnerId, int ownerBattleWins,
      int attackerBattleWins, int numHoursForBattle, Date lastRewardGiven,
      Date timeOfEntry, String reasonForEntry) {
    super();
    this.clanOwnerId = clanOwnerId;
    this.clanAttackerId = clanAttackerId;
    this.towerId = towerId;
    this.attackStartTime = attackStartTime;
    this.winnerId = winnerId;
    this.ownerBattleWins = ownerBattleWins;
    this.attackerBattleWins = attackerBattleWins;
    this.numHoursForBattle = numHoursForBattle;
    this.lastRewardGiven = lastRewardGiven;
    this.timeOfEntry = timeOfEntry;
    this.reasonForEntry = reasonForEntry;
  }

  public int getTowerId() {
    return towerId;
  }

  public void setTowerId(int towerId) {
    this.towerId = towerId;
  }

  public int getWinnerId() {
    return winnerId;
  }

  public void setWinnerId(int winnerId) {
    this.winnerId = winnerId;
  }

  public Date getTimeOfEntry() {
    return timeOfEntry;
  }

  public void setTimeOfEntry(Date timeOfEntry) {
    this.timeOfEntry = timeOfEntry;
  }

  public String getReasonForEntry() {
    return reasonForEntry;
  }

  public void setReasonForEntry(String reasonForEntry) {
    this.reasonForEntry = reasonForEntry;
  }

  public int getClanOwnerId() {
    return clanOwnerId;
  }

  public void setClanOwnerId(int clanOwnerId) {
    this.clanOwnerId = clanOwnerId;
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
  public Date getLastRewardGiven() {
    return lastRewardGiven;
  }

  public void setLastRewardGiven(Date lastRewardGiven) {
    this.lastRewardGiven = lastRewardGiven;
  }

  @Override
  public String toString() {
    return "ClanTowerHistory [clanOwnerId=" + clanOwnerId + ", clanAttackerId="
        + clanAttackerId + ", towerId=" + towerId + ", attackStartTime="
        + attackStartTime + ", winnerId=" + winnerId + ", ownerBattleWins="
        + ownerBattleWins + ", attackerBattleWins=" + attackerBattleWins
        + ", numHoursForBattle=" + numHoursForBattle + ", lastRewardGiven="
        + lastRewardGiven + ", timeOfEntry=" + timeOfEntry
        + ", reasonForEntry=" + reasonForEntry + "]";
  }

  public int getNumHoursForBattle() {
    return numHoursForBattle;
  }

  public void setNumHoursForBattle(int numHoursForBattle) {
    this.numHoursForBattle = numHoursForBattle;
  }
}
