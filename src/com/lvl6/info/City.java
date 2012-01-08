package com.lvl6.info;

public class City {

  private int id;
  private String name;
  private int minLevel;
  private int expGainedBaseOnRankup;
  private int coinsGainedBaseOnRankup;
  
  public City(int id, String name, int minLevel, int expGainedBaseOnRankup,
      int coinsGainedBaseOnRankup) {
    this.id = id;
    this.name = name;
    this.minLevel = minLevel;
    this.expGainedBaseOnRankup = expGainedBaseOnRankup;
    this.coinsGainedBaseOnRankup = coinsGainedBaseOnRankup;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getMinLevel() {
    return minLevel;
  }

  public int getExpGainedBaseOnRankup() {
    return expGainedBaseOnRankup;
  }

  public int getCoinsGainedBaseOnRankup() {
    return coinsGainedBaseOnRankup;
  }  
}
