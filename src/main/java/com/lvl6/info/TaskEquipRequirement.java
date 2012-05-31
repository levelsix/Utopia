package com.lvl6.info;

public class TaskEquipRequirement {

  private int taskId;
  private int equipId;
  private int quantity;
  
  public TaskEquipRequirement(int taskId, int equipId, int quantity) {
    this.taskId = taskId;
    this.equipId = equipId;
    this.quantity = quantity;
  }

  public int getTaskId() {
    return taskId;
  }

  public int getEquipId() {
    return equipId;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return "TaskEquipRequirement [taskId=" + taskId + ", equipId=" + equipId
        + ", quantity=" + quantity + "]";
  }
  
}
