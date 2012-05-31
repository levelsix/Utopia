package com.lvl6.info;

public class UserEquip {

  private int userId;
  private int equipId;
  private int quantity;
  
  public UserEquip(int userId, int equipId, int quantity) {
    this.userId = userId;
    this.equipId = equipId;
    this.quantity = quantity;
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

  @Override
  public String toString() {
    return "UserEquip [userId=" + userId + ", equipId=" + equipId
        + ", quantity=" + quantity + "]";
  }

}
