package com.lvl6.info;

import java.util.Date;

import com.lvl6.proto.InfoProto.BattleResult;

public class BattleDetails {
  private int attackerId;
  private int defenderId;
  private BattleResult result;
  private Date battleCompleteTime;
  private int coinsStolen;
  private int equipStolen;
  private int expGained;
  public BattleDetails(int attackerId, int defenderId, BattleResult result,
      Date battleCompleteTime, int coinsStolen, int equipStolen, int expGained) {
    this.attackerId = attackerId;
    this.defenderId = defenderId;
    this.result = result;
    this.battleCompleteTime = battleCompleteTime;
    this.coinsStolen = coinsStolen;
    this.equipStolen = equipStolen;
    this.expGained = expGained;
  }
  public int getAttackerId() {
    return attackerId;
  }
  public int getDefenderId() {
    return defenderId;
  }
  public BattleResult getResult() {
    return result;
  }
  public Date getBattleCompleteTime() {
    return battleCompleteTime;
  }
  public int getCoinsStolen() {
    return coinsStolen;
  }
  public int getEquipStolen() {
    return equipStolen;
  }
  public int getExpGained() {
    return expGained;
  }
  @Override
  public String toString() {
    return "BattleDetails [attackerId=" + attackerId + ", defenderId="
        + defenderId + ", result=" + result + ", battleCompleteTime="
        + battleCompleteTime + ", coinsStolen=" + coinsStolen
        + ", equipStolen=" + equipStolen + ", expGained=" + expGained + "]";
  }
}
