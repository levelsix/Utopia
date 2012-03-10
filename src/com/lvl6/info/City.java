package com.lvl6.info;

public class City {

  private int id;
  private String name;
  private int minLevel;
  private int expGainedBaseOnRankup;
  private int coinsGainedBaseOnRankup;
  private String mapImgName;
  private CoordinatePair aviaryCoords;
  private CoordinatePair spriteAviaryLandingCoords;
  public City(int id, String name, int minLevel, int expGainedBaseOnRankup,
      int coinsGainedBaseOnRankup, String mapImgName,
      CoordinatePair aviaryCoords, CoordinatePair spriteAviaryLandingCoords) {
    this.id = id;
    this.name = name;
    this.minLevel = minLevel;
    this.expGainedBaseOnRankup = expGainedBaseOnRankup;
    this.coinsGainedBaseOnRankup = coinsGainedBaseOnRankup;
    this.mapImgName = mapImgName;
    this.aviaryCoords = aviaryCoords;
    this.spriteAviaryLandingCoords = spriteAviaryLandingCoords;
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
  public String getMapImgName() {
    return mapImgName;
  }
  public CoordinatePair getAviaryCoords() {
    return aviaryCoords;
  }
  public CoordinatePair getSpriteAviaryLandingCoords() {
    return spriteAviaryLandingCoords;
  }
  @Override
  public String toString() {
    return "City [id=" + id + ", name=" + name + ", minLevel=" + minLevel
        + ", expGainedBaseOnRankup=" + expGainedBaseOnRankup
        + ", coinsGainedBaseOnRankup=" + coinsGainedBaseOnRankup
        + ", mapImgName=" + mapImgName + ", aviaryCoords=" + aviaryCoords
        + ", spriteAviaryLandingCoords=" + spriteAviaryLandingCoords + "]";
  }
  
}
