package com.lvl6.info;

import java.io.Serializable;

public class UserEquip implements Serializable {

	private static final long serialVersionUID = 7780574642033448043L;
	private int id;
	private int userId;
	private int equipId;
	private int level;
  private int enhancementPercentage;

	public UserEquip(int id, int userId, int equipId, int level, int enhancementPercentage) {
    this.id = id;
    this.userId = userId;
    this.equipId = equipId;
    this.level = level;
    this.enhancementPercentage = enhancementPercentage;
  }

	public int getEnhancementPercentage() {
    return enhancementPercentage;
  }

  public void setEnhancementPercentage(int enhancementPercentage) {
    this.enhancementPercentage = enhancementPercentage;
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
	
  public int getLevel() {
    return level;
  }

  @Override
  public String toString() {
    return "UserEquip [id=" + id + ", userId=" + userId + ", equipId="
        + equipId + ", level=" + level + ", enhancementPercentage="
        + enhancementPercentage + "]";
  }

}
