package com.lvl6.info;

import java.util.Date;

import com.lvl6.proto.InfoProto.ExpansionDirection;

public class UserCityExpansionData {
  private int userId;
  private int nearLeftExpansions;
  private int farLeftExpansions;
  private int farRightExpansions;
  private boolean isExpanding;
  private Date lastExpandTime;          //refers to last time the user clicks the upgrade button, not when the last upgrade was complete
  private ExpansionDirection lastExpandDirection;
  public UserCityExpansionData(int userId, int nearLeftExpansions, int farLeftExpansions,
      int farRightExpansions, boolean isExpanding, Date lastExpandTime,
      ExpansionDirection lastExpandDirection) {
    this.userId = userId;
    this.nearLeftExpansions = nearLeftExpansions;
    this.farLeftExpansions = farLeftExpansions;
    this.farRightExpansions = farRightExpansions;
    this.isExpanding = isExpanding;
    this.lastExpandTime = lastExpandTime;
    this.lastExpandDirection = lastExpandDirection;
  }
  public int getUserId() {
    return userId;
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
  
  public int getTotalNumCompletedExpansions() {
    return nearLeftExpansions + farLeftExpansions + farRightExpansions;
  }
  
  @Override
  public String toString() {
    return "UserCityExpansionData [userId=" + userId + ", nearLeftExpansions="
        + nearLeftExpansions + ", farLeftExpansions=" + farLeftExpansions
        + ", farRightExpansions=" + farRightExpansions + ", isExpanding="
        + isExpanding + ", lastExpandTime=" + lastExpandTime
        + ", lastExpandDirection=" + lastExpandDirection + "]";
  }  
}
