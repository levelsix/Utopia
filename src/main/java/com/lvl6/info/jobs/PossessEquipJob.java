package com.lvl6.info.jobs;

public class PossessEquipJob {
  private int id;
  private int equipId;
  private int quantity;
  
  public PossessEquipJob(int id, int equipId, int quantity) {
    this.id = id;
    this.equipId = equipId;
    this.quantity = quantity;
  }

  public int getId() {
    return id;
  }

  public int getEquipId() {
    return equipId;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "PossessEquipJob [id=" + id + ", equipId=" + equipId + ", quantity="
        + quantity + "]";
  }
}
