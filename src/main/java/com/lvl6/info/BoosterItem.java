package com.lvl6.info;

import java.io.Serializable;

public class BoosterItem implements Serializable {
  private static final long serialVersionUID = -8696581640525315061L;
  private int id;
  private int boosterPackId;
  private int equipId;
  private int quantity;
  private boolean isSpecial;
  
  public BoosterItem(int id, int boosterPackId, int equipId, 
      int quantity, boolean isSpecial) {
    super();
    this.id = id;
    this.boosterPackId = boosterPackId;
    this.equipId = equipId;
    this.quantity = quantity;
    this.isSpecial = isSpecial;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBoosterPackId() {
    return boosterPackId;
  }

  public void setBoosterPackId(int boosterPackId) {
    this.boosterPackId = boosterPackId;
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

  public boolean isSpecial() {
    return isSpecial;
  }

  public void setSpecial(boolean isSpecial) {
    this.isSpecial = isSpecial;
  }

  @Override
  public String toString() {
    return "BoosterItem [id=" + id + ", boosterPackId=" + boosterPackId
        + ", equipId=" + equipId + ", quantity=" + quantity + ", isSpecial="
        + isSpecial + "]";
  }
  
}
