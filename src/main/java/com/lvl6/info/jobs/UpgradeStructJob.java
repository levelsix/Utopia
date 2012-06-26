package com.lvl6.info.jobs;

public class UpgradeStructJob {
  private int id;
  private int structId;
  private int levelReq;
  
  public UpgradeStructJob(int id, int structId, int levelReq) {
    this.id = id;
    this.structId = structId;
    this.levelReq = levelReq;
  }
  public int getId() {
    return id;
  }
  public int getStructId() {
    return structId;
  }
  public int getLevelReq() {
    return levelReq;
  }
  @Override
  public String toString() {
    return "UpgradeStructJob [id=" + id + ", structId=" + structId
        + ", levelReq=" + levelReq + "]";
  }
}
