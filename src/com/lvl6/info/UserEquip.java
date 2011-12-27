package com.lvl6.info;

public class UserEquip {

  private int userId;
  private int equipId;
  private int quantity;
  private boolean isStolen;
  
  public UserEquip(int userId, int equipId, int quantity, boolean isStolen) {
    this.userId = userId;
    this.equipId = equipId;
    this.quantity = quantity;
    this.isStolen = isStolen;
  }
  
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getEquipId() {
    return equipId;
  }
  public void setEquipId(int equipId) {
    this.equipId = equipId;
  }
  public int getQuantity() {
    return quantity;
  }
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
  public boolean isStolen() {
    return isStolen;
  }
  public void setStolen(boolean isStolen) {
    this.isStolen = isStolen;
  }

  @Override
  public String toString() {
    return "UserEquip [userId=" + userId + ", equipId=" + equipId
        + ", quantity=" + quantity + ", isStolen=" + isStolen + "]";
  }
}
