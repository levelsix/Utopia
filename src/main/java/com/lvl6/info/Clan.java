package com.lvl6.info;

import java.util.Date;

public class Clan {
  private int id;
  private String name;
  private int ownerId;
  private Date createTime;
  private String description;
  private String tag;
  private boolean isGood;
  private int currentTierLevel;
  private boolean requestToJoinRequired;
  
  public Clan(int id, String name, int ownerId, Date createTime,
      String description, String tag, boolean isGood, int tier,
      boolean requestToJoinRequired) {
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.createTime = createTime;
    this.description = description;
    this.tag = tag;
    this.isGood = isGood;
    this.currentTierLevel = tier;
    this.requestToJoinRequired = requestToJoinRequired; 
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public String getDescription() {
    return description;
  }

  public String getTag() {
    return tag;
  }

  public boolean isGood() {
    return isGood;
  }
  
  public int getCurrentTierLevel() {
	  return currentTierLevel;
  }

  public boolean isRequestToJoinRequired() {
    return requestToJoinRequired;
  }

  public void setRequestToJoinRequired(boolean requestToJoinRequired) {
    this.requestToJoinRequired = requestToJoinRequired;
  }

  @Override
  public String toString() {
    return "Clan [id=" + id + ", name=" + name + ", ownerId=" + ownerId
        + ", createTime=" + createTime + ", description=" + description
        + ", tag=" + tag + ", isGood=" + isGood + ", currentTierLevel="
        + currentTierLevel + ", requestToJoinRequired=" + requestToJoinRequired
        + "]";
  }
}

