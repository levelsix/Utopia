package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

import com.lvl6.proto.InfoProto.BattleResult;

public class BattleDetails implements Serializable {

	private static final long serialVersionUID = 7523602709627035018L;
	private int attackerId;
	private int defenderId;
	private BattleResult result;
	private Date battleCompleteTime;
	private int coinsStolen;
	private int equipStolen;
	private int expGained;
	private int stolenEquipLevel;

	public BattleDetails(int attackerId, int defenderId, BattleResult result,
      Date battleCompleteTime, int coinsStolen, int equipStolen, int expGained,
      int stolenEquipLevel) {
    this.attackerId = attackerId;
    this.defenderId = defenderId;
    this.result = result;
    this.battleCompleteTime = battleCompleteTime;
    this.coinsStolen = coinsStolen;
    this.equipStolen = equipStolen;
    this.expGained = expGained;
    this.stolenEquipLevel = stolenEquipLevel;
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

  public int getStolenEquipLevel() {
    return stolenEquipLevel;
  }

  @Override
  public String toString() {
    return "BattleDetails [attackerId=" + attackerId + ", defenderId="
        + defenderId + ", result=" + result + ", battleCompleteTime="
        + battleCompleteTime + ", coinsStolen=" + coinsStolen
        + ", equipStolen=" + equipStolen + ", expGained=" + expGained
        + ", stolenEquipLevel=" + stolenEquipLevel + "]";
  }
}
