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
    this.isStolen = isStolen;   //from other players in battle, and for missions
  }
  
  public int getUserId() {
    return userId;
  }


  public int getEquipId() {
    return equipId;
  }


  public synchronized int getQuantity() {
    return quantity;
  }


  public boolean isStolen() {
    return isStolen;
  }


  @Override
  public String toString() {
    return "UserEquip [userId=" + userId + ", equipId=" + equipId
        + ", quantity=" + quantity + ", isStolen=" + isStolen + "]";
  }
}
