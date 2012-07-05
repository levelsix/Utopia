package com.lvl6.info;

public class UserEquip {

  private int userEquipId;
  private int userId;
  private int equipId;
  
  public UserEquip(int userEquipId, int userId, int equipId) {
    this.userEquipId = userEquipId;
    this.userId = userId;
    this.equipId = equipId;
  }

  public int getUserEquipId() {
    return userEquipId;
  }
  public int getUserId() {
    return userId;
  }
  public int getEquipId() {
    return equipId;
  }

  @Override
  public String toString() {
    return "UserEquip [userEquipId=" + userEquipId + ", userId=" + userId
        + ", equipId=" + equipId + "]";
  }
  
}
