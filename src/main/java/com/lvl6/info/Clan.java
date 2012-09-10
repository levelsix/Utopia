package com.lvl6.info;

import java.util.Date;

public class Clan {
  private int clanId;
  private String name;
  private int ownerId;
  private Date createTime;
  public Clan(int clanId, String name, int ownerId, Date createTime) {
    this.clanId = clanId;
    this.name = name;
    this.ownerId = ownerId;
    this.createTime = createTime;
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
  @Override
  public String toString() {
    return "Clan [clanId=" + clanId + ", name=" + name + ", ownerId=" + ownerId
        + ", createTime=" + createTime + "]";
  }


}

