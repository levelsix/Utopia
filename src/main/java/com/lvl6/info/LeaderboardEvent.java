package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class LeaderboardEvent implements Serializable {
  private static final long serialVersionUID = -3130246899148578214L;
  private int id;
  private Date startDate;
  private Date endDate;
  private String eventName;
  
  public LeaderboardEvent(int id, Date startDate, Date endDate, String eventName) {
    super();
    this.id = id;
    this.startDate = startDate;
    this.endDate = endDate;
    this.eventName = eventName;
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
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  @Override
  public String toString() {
    return "LeaderboardEvent [id=" + id + ", startDate=" + startDate 
        + ", endDate=" + endDate + ", eventName=" + eventName + "]";
  }
}