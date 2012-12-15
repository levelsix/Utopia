package com.lvl6.info;

import java.io.Serializable;

public class LeaderboardEventReward implements Serializable {
  private static final long serialVersionUID = -7235506292340192848L;
  private int leaderboardEventId;
  private int minRank;
  private int maxRank;
  private int goldRewarded;
  private String backgroundImageName;
  private String prizeImageName;
  private int blue;
  private int green;
  private int red;
  
  public LeaderboardEventReward(int leaderboardEventId, int minRank, int maxRank, int goldRewarded,
      String backgroundImageName, String prizeImageName, int blue, int green, int red) {
    super();
    this.leaderboardEventId = leaderboardEventId;
    this.minRank = minRank;
    this.maxRank = maxRank;
    this.goldRewarded = goldRewarded;
    this.backgroundImageName = backgroundImageName;
    this.prizeImageName = prizeImageName;
    this.blue = blue;
    this.green = green;
    this.red = red;
  }

  public int getLeaderboardEventId() {
    return leaderboardEventId;
  }
  public void setLeaderboardEventId(int leaderboardEventId) {
    this.leaderboardEventId = leaderboardEventId;
  }
  public int getMinRank() {
    return minRank;
  }
  public void setMinRank(int minRank) {
    this.minRank = minRank;
  }
  public int getMaxRank() {
    return maxRank;
  }
  public void setMaxRank(int maxRank) {
    this.maxRank = maxRank;
  }
  public int getGoldRewarded() {
    return goldRewarded;
  }
  public void setGoldRewarded(int goldRewarded) {
    this.goldRewarded = goldRewarded;
  }
  public String getBackgroundImageName() {
    return backgroundImageName;
  }
  public void setBackgroundImageName(String backgroundImageName) {
    this.backgroundImageName = backgroundImageName;
  }
  public String getPrizeImageName() {
    return prizeImageName;
  }
  public void setPrizeImageName(String prizeImageName) {
    this.prizeImageName = prizeImageName;
  }
  public int getBlue() {
    return blue;
  }
  public void setBlue(int blue) {
    this.blue = blue;
  }
  public int getGreen() {
    return green;
  }
  public void setGreen(int green) {
    this.green = green;
  }
  public int getRed() {
    return red;
  }
  public void setRed(int red) {
    this.red = red;
  }

  @Override
  public String toString() {
    return "LeaderboardEvent [leaderboardEventId=" + leaderboardEventId + ", minRank=" + minRank 
        + ", maxRank=" + maxRank + ", goldRewarded=" + goldRewarded
        + ", backgroundImageName=" + backgroundImageName + ", prizeImageName=" + prizeImageName
        + ", blue=" + blue + ", green=" + green + ", red=" + red + "]";
  }
}