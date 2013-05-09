package com.lvl6.info;

public class UserLockBoxItem {
  
  private int lockBoxItemId;
  private int userId;
  private int quantity;
  private boolean hasBeenRedeemed;
  
  public UserLockBoxItem(int lockBoxItemId, int userId,
      int quantity, boolean hasBeenRedeemed) {
   
    this.lockBoxItemId = lockBoxItemId;
    this.userId = userId;
    this.quantity = quantity;
    this.hasBeenRedeemed = hasBeenRedeemed;
  }

  public int getLockBoxItemId() {
    return lockBoxItemId;
  }

  public void setLockBoxItemId(int lockBoxItemId) {
    this.lockBoxItemId = lockBoxItemId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public boolean isHasBeenRedeemed() {
    return hasBeenRedeemed;
  }

  public void setHasBeenRedeemed(boolean hasBeenRedeemed) {
    this.hasBeenRedeemed = hasBeenRedeemed;
  }

  @Override
  public String toString() {
    return "UserLockBoxItem [lockBoxItemId=" + lockBoxItemId + ", userId="
        + userId + ", quantity=" + quantity + ", hasBeenRedeemed="
        + hasBeenRedeemed + "]";
  }
  
  
}