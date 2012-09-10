package com.lvl6.info;

import java.util.Date;

public class Clan {
  private int clanId;
  private String name;
  private int ownerId;
  private Date createTime;
  private String description;
  
  public Clan(int clanId, String name, int ownerId, Date createTime,
      String description) {
    this.clanId = clanId;
    this.name = name;
    this.ownerId = ownerId;
    this.createTime = createTime;
    this.description = description;
  }

  public int getClanId() {
    return clanId;
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
    return "Clan [clanId=" + clanId + ", name=" + name + ", ownerId=" + ownerId
        + ", createTime=" + createTime + ", description=" + description + "]";
  }
  
}

