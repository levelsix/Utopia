package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class LockBoxEvent implements Serializable {
  private static final long serialVersionUID = -368747979562792778L;
  private int id;
  private Date startDate;
  private Date endDate;
  private String lockBoxImageName;
  private String eventName;
  private int prizeEquipId;
  
  public LockBoxEvent(int id, Date startDate, Date endDate,
      String lockBoxImageName, String eventName, int prizeEquipId) {
    super();
    this.id = id;
    this.startDate = startDate;
    this.endDate = endDate;
    this.lockBoxImageName = lockBoxImageName;
    this.eventName = eventName;
    this.prizeEquipId = prizeEquipId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getLockBoxImageName() {
    return lockBoxImageName;
  }

  public void setLockBoxImageName(String lockBoxImageName) {
    this.lockBoxImageName = lockBoxImageName;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public int getPrizeEquipId() {
    return prizeEquipId;
  }

  public void setPrizeEquipId(int prizeEquipId) {
    this.prizeEquipId = prizeEquipId;
  }

  @Override
  public String toString() {
    return "LockBoxEvent [id=" + id + ", startDate=" + startDate + ", endDate="
        + endDate + ", lockBoxImageName=" + lockBoxImageName + ", eventName="
        + eventName + ", prizeEquipId=" + prizeEquipId + "]";
  }
}