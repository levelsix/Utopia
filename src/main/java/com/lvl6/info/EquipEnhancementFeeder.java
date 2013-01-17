package com.lvl6.info;

import java.io.Serializable;

public class EquipEnhancementFeeder implements Serializable {
  

  private static final long serialVersionUID = 105567085058423780L;
  private int id;
  private int equipEnhancementId;
  private int equipId;
  private int equipLevel;
  private int enhancementPercentageBeforeEnhancement;
  
  public EquipEnhancementFeeder(int id, int equipEnhancementId, int equipId, int equipLevel, 
      int enhancementPercentageBeforeEnhancement) {
    this.equipEnhancementId = equipEnhancementId;
    this.equipId = equipId;
    this.equipLevel = equipLevel;
    this.enhancementPercentageBeforeEnhancement = enhancementPercentageBeforeEnhancement;
  }
  
  public int getId() {
    return id;
  }

  public int getEquipEnhancementId() {
    return equipEnhancementId;
  }

  public int getEquipId() {
    return equipId;
  }

  public int getEquipLevel() {
    return equipLevel;
  }

  public int getEnhancementPercentageBeforeEnhancement() {
    return enhancementPercentageBeforeEnhancement;
  }

  @Override
  public String toString() {
    return "EquipEnhancementFeeder [id=" + id + ", equipEnhancementId="
        + equipEnhancementId + ", equipId=" + equipId + ", equipLevel="
        + equipLevel + ", enhancementPercentageBeforeEnhancement="
        + enhancementPercentageBeforeEnhancement + "]";
  }

  
}
