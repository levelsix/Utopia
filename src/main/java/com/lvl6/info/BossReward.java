package com.lvl6.info;

import java.io.Serializable;

public class BossReward implements Serializable {

	private static final long serialVersionUID = 2003432144838192811L;
	private int id;
	private int bossId;
	private int minSilver;
	private int maxSilver;
	private int minGold;
	private int maxGold;
	private int equipId;
	private float probabilityToBeAwarded;
	private int rewardGroup;

	public BossReward(int id, int bossId, int minSilver, int maxSilver, int minGold,
			int maxGold, int equipId, float probabilityToBeAwarded, int rewardGroup) {
    this.id = id;
    this.bossId = bossId;
    this.minSilver = minSilver;
    this.maxSilver = maxSilver;
    this.minGold = minGold;
    this.maxGold = maxGold;
    this.equipId = equipId;
    this.probabilityToBeAwarded = probabilityToBeAwarded;
    this.rewardGroup = rewardGroup;
  }

  public int getId() {
	  return id;
  }

  public int getBossId() {
	  return bossId;
  }

  public int getMinSilver() {
	  return minSilver;
  }
	
  public int getMaxSilver() {
    return maxSilver;
  }

  public int getMinGold() {
	  return minGold;
  }
  
  public int getMaxGold() {
	  return maxGold;
  }
  
  public int getEquipId() {
	  return equipId;
  }
  
  public float getProbabilityToBeAwarded() {
	  return probabilityToBeAwarded;
  }
  
  public int getRewardGroup() {
	  return rewardGroup;
  }
  
  @Override
  public String toString() {
    return "UserEquip [id=" + id + ", bossId=" + bossId + ", minSilver="
        + minSilver + ", maxSilver=" + maxSilver + ", minGold=" + minGold
        + "maxGold" + maxGold + ", equipId" + equipId + ", probabilityToBeAwarded="
        + probabilityToBeAwarded + ", rewardGroup=" + rewardGroup + "]";
  }

}
