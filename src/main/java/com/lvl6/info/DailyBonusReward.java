package com.lvl6.info;

import java.util.List;

public class DailyBonusReward {
  
  private int id;
  private int minLevel;
  private int maxLevel;
  private int dayOneCoins;
  private int dayTwoCoins;
  private int dayThreeDiamonds;
  private int dayFourCoins;
  private List<Integer> dayFiveBoosterPackIds;
  
  public DailyBonusReward(int id, int minLevel, int maxLevel, int dayOneCoins,
      int dayTwoCoins, int dayThreeDiamonds, int dayFourCoins,
      List<Integer> dayFiveBoosterPackIds) {
    super();
    this.id = id;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.dayOneCoins = dayOneCoins;
    this.dayTwoCoins = dayTwoCoins;
    this.dayThreeDiamonds = dayThreeDiamonds;
    this.dayFourCoins = dayFourCoins;
    this.dayFiveBoosterPackIds = dayFiveBoosterPackIds;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getMinLevel() {
    return minLevel;
  }

  public void setMinLevel(int minLevel) {
    this.minLevel = minLevel;
  }

  public int getMaxLevel() {
    return maxLevel;
  }

  public void setMaxLevel(int maxLevel) {
    this.maxLevel = maxLevel;
  }

  public int getDayOneCoins() {
    return dayOneCoins;
  }

  public void setDayOneCoins(int dayOneCoins) {
    this.dayOneCoins = dayOneCoins;
  }

  public int getDayTwoCoins() {
    return dayTwoCoins;
  }

  public void setDayTwoCoins(int dayTwoCoins) {
    this.dayTwoCoins = dayTwoCoins;
  }

  public int getDayThreeDiamonds() {
    return dayThreeDiamonds;
  }

  public void setDayThreeDiamonds(int dayThreeDiamonds) {
    this.dayThreeDiamonds = dayThreeDiamonds;
  }

  public int getDayFourCoins() {
    return dayFourCoins;
  }

  public void setDayFourCoins(int dayFourCoins) {
    this.dayFourCoins = dayFourCoins;
  }

  public List<Integer> getDayFiveBoosterPackIds() {
    return dayFiveBoosterPackIds;
  }

  public void setDayFiveBoosterPackIds(List<Integer> dayFiveBoosterPackIds) {
    this.dayFiveBoosterPackIds = dayFiveBoosterPackIds;
  }

  @Override
  public String toString() {
    return "DailyBonusReward [id=" + id + ", minLevel=" + minLevel
        + ", maxLevel=" + maxLevel + ", dayOneCoins=" + dayOneCoins
        + ", dayTwoCoins=" + dayTwoCoins + ", dayThreeDiamonds="
        + dayThreeDiamonds + ", dayFourCoins=" + dayFourCoins
        + ", dayFiveBoosterPackIds=" + dayFiveBoosterPackIds + "]";
  }
  
}
