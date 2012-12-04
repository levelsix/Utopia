package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class BossEvent implements Serializable {
  private static final long serialVersionUID = 828184315386406610L;
  private int id;
  private int bossId;
  private Date startDate;
  private Date endDate;
  private String bossImageName;
  private String eventName;
  
  public BossEvent(int id, int bossId, Date startDate, Date endDate,
      String bossImageName, String eventName) {
    super();
    this.id = id;
    this.bossId = bossId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.bossImageName = bossImageName;
    this.eventName = eventName;
    
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBossId() {
	  return this.bossId;
  }
  
  public void setBossId(int bossId) {
	  this.bossId = bossId;
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

  public String getBossImageName() {
    return bossImageName;
  }

  public void setLockBoxImageName(String bossImageName) {
    this.bossImageName = bossImageName;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  @Override
  public String toString() {
    return "BossEvent [id=" + id + ", bossId=" + bossId + ", startDate="
        + startDate + ", endDate=" + endDate + ", bossImageName="
        + bossImageName + ", eventName=" + eventName + "]";
  }
}