package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class EquipEnhancement implements Serializable {
  

  private static final long serialVersionUID = -8339827445180289779L;
  private int id;
  private int userId;
  private int equipId;
  private int equipLevel;
  private int enhancementPercentage;
  private Date startTimeOfEnhancement;
  
  public EquipEnhancement(int id, int userId, int equipId, int equipLevel, 
      int enhancementPercentage, Date startTimeOfEnhancement) {
    this.id = id;
    this.userId = userId;
    this.equipId = equipId;
    this.equipLevel = equipLevel;
    this.enhancementPercentage = enhancementPercentage;
    this.startTimeOfEnhancement = startTimeOfEnhancement;
  }

  public int getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public int getEquipId() {
    return equipId;
  }

  public int getEquipLevel() {
    return equipLevel;
  }

  public int getEnhancementPercentage() {
    return enhancementPercentage;
  }

  public Date getStartTimeOfEnhancement() {
    return startTimeOfEnhancement;
  }

  @Override
  public String toString() {
    return "EquipEnhancement [id=" + id + ", userId=" + userId + ", equipId="
        + equipId + ", equipLevel=" + equipLevel + ", enhancementPercentage="
        + enhancementPercentage + ", startTimeOfEnhancement="
        + startTimeOfEnhancement + "]";
  }
  
}
