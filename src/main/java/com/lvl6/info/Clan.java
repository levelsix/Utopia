package com.lvl6.info;

import java.util.Date;

public class Clan {
  private int id;
  private String name;
  private int ownerId;
  private Date createTime;
  private String description;
  
  public Clan(int id, String name, int ownerId, Date createTime,
      String description) {
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.createTime = createTime;
    this.description = description;
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

  @Override
  public String toString() {
    return "Clan [clanId=" + id + ", name=" + name + ", ownerId=" + ownerId
        + ", createTime=" + createTime + ", description=" + description + "]";
  }
  
}

