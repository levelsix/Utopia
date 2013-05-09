package com.lvl6.info;

import java.io.Serializable;

import com.lvl6.proto.InfoProto.EquipClassType;

public class LockBoxItem implements Serializable {
  private static final long serialVersionUID = -3095335614207787380L;
  private int id;
  private int lockBoxEventId;
  private float chanceToUnlock;
  private String name;
  private EquipClassType classType;
  private String imageName;
  private int redeemForNumBoosterItems;
  private boolean isGoldBoosterPack;
  
  public LockBoxItem(int id, int lockBoxEventId, float chanceToUnlock,
      String name, EquipClassType classType, String imageName,
      int redeemForNumBoosterItems, boolean isGoldBoosterPack) {
    super();
    this.id = id;
    this.lockBoxEventId = lockBoxEventId;
    this.chanceToUnlock = chanceToUnlock;
    this.name = name;
    this.classType = classType;
    this.imageName = imageName;
    this.redeemForNumBoosterItems = redeemForNumBoosterItems;
    this.isGoldBoosterPack = isGoldBoosterPack;
  }
  public String getImageName() {
    return imageName;
  }
  public void setImageName(String imageName) {
    this.imageName = imageName;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public int getLockBoxEventId() {
    return lockBoxEventId;
  }
  public void setLockBoxEventId(int lockBoxEventId) {
    this.lockBoxEventId = lockBoxEventId;
  }
  public float getChanceToUnlock() {
    return chanceToUnlock;
  }
  public void setChanceToUnlock(float chanceToUnlock) {
    this.chanceToUnlock = chanceToUnlock;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public EquipClassType getClassType() {
    return classType;
  }
  public void setClassType(EquipClassType classType) {
    this.classType = classType;
  }
  public int getRedeemForNumBoosterItems() {
    return redeemForNumBoosterItems;
  }
  public void setRedeemForNumBoosterItems(int redeemForNumBoosterItems) {
    this.redeemForNumBoosterItems = redeemForNumBoosterItems;
  }
  public boolean isGoldBoosterPack() {
    return isGoldBoosterPack;
  }
  public void setGoldBoosterPack(boolean isGoldBoosterPack) {
    this.isGoldBoosterPack = isGoldBoosterPack;
  }
  
  @Override
  public String toString() {
    return "LockBoxItem [id=" + id + ", lockBoxEventId=" + lockBoxEventId
        + ", chanceToUnlock=" + chanceToUnlock + ", name=" + name
        + ", classType=" + classType + ", imageName=" + imageName
        + ", redeemForNumBoosterItems=" + redeemForNumBoosterItems
        + ", isGoldBoosterPack=" + isGoldBoosterPack + "]";
  }
}
