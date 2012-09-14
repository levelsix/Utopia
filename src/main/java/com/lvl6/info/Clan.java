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
  
  public Clan(int id, String name, int ownerId, Date createTime,
      String description, String tag, boolean isGood) {
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.createTime = createTime;
    this.description = description;
    this.tag = tag;
    this.isGood = isGood;
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

  @Override
  public String toString() {
    return "Clan [id=" + id + ", name=" + name + ", ownerId=" + ownerId
        + ", createTime=" + createTime + ", description=" + description
        + ", tag=" + tag + ", isGood=" + isGood + "]";
  }

}

