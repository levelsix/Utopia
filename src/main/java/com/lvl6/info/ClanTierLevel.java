package com.lvl6.info;

import java.io.Serializable;
//TODO: determine if implementing Serializable is necessary
public class ClanTierLevel implements Serializable {
  /**
	 * 
	 */
  private static final long serialVersionUID = -7324323478279055948L;
  private int tierLevel;
  private int clanSize;
  private int goldCostToUpgradeToNextTierLevel;

  public ClanTierLevel(int tierLevel, int maxClanSize, int goldCostToUpgradeToNextTier) {
    super();
    this.tierLevel = tierLevel;
    this.clanSize = maxClanSize;
    this.goldCostToUpgradeToNextTierLevel = goldCostToUpgradeToNextTier;
  }
  
  public int getTierLevel() {
    return tierLevel;
  }
  public void setTierLevel(int tierLevel) {
    this.tierLevel = tierLevel;
  }
  public int getMaxClanSize() {
    return clanSize;
  }
  public void setMaxClanSize(int clanSize) {
    this.clanSize = clanSize;
  }
  public int getGoldCostToUpgradeToNextTierLevel() {
    return goldCostToUpgradeToNextTierLevel;
  }
  public void setGoldCostToUpgradeToNextTierLevel(int tierUpgradeGoldCost) {
    this.goldCostToUpgradeToNextTierLevel = tierUpgradeGoldCost;
  }
  @Override
  public String toString() {
    return "UserBoss [tierLevel=" + tierLevel + ", clanSize=" + clanSize
        + ", tierUpgradeGoldCost=" + goldCostToUpgradeToNextTierLevel + "]";
  }
}
