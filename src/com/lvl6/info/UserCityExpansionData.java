package com.lvl6.info;

import java.util.Date;

import com.lvl6.proto.InfoProto.ExpansionDirection;

public class UserCityExpansionData {
  private int nearLeftExpansions;
  private int farLeftExpansions;
  private int farRightExpansions;
  private boolean isExpanding;
  private Date lastExpandTime;
  private ExpansionDirection lastExpandDirection;
  public UserCityExpansionData(int nearLeftExpansions, int farLeftExpansions,
      int farRightExpansions, boolean isExpanding, Date lastExpandTime,
      ExpansionDirection lastExpandDirection) {
    this.nearLeftExpansions = nearLeftExpansions;
    this.farLeftExpansions = farLeftExpansions;
    this.farRightExpansions = farRightExpansions;
    this.isExpanding = isExpanding;
    this.lastExpandTime = lastExpandTime;
    this.lastExpandDirection = lastExpandDirection;
  }
  public int getNearLeftExpansions() {
    return nearLeftExpansions;
  }
  public int getFarLeftExpansions() {
    return farLeftExpansions;
  }
  public int getFarRightExpansions() {
    return farRightExpansions;
  }
  public boolean isExpanding() {
    return isExpanding;
  }
  public Date getLastExpandTime() {
    return lastExpandTime;
  }
  public ExpansionDirection getLastExpandDirection() {
    return lastExpandDirection;
  }
  @Override
  public String toString() {
    return "UserCityExpansionData [nearLeftExpansions=" + nearLeftExpansions
        + ", farLeftExpansions=" + farLeftExpansions + ", farRightExpansions="
        + farRightExpansions + ", isExpanding=" + isExpanding
        + ", lastExpandTime=" + lastExpandTime + ", lastExpandDirection="
        + lastExpandDirection + "]";
  }
}
