package com.lvl6.info.jobs;

import com.lvl6.proto.InfoProto.MarketplaceJobRequirementType;

public class MarketplaceJob {
  private int id;
  private MarketplaceJobRequirementType actionReq;
  public MarketplaceJob(int id, MarketplaceJobRequirementType actionReq) {
    this.id = id;
    this.actionReq = actionReq;
  }
  public int getId() {
    return id;
  }
  public MarketplaceJobRequirementType getActionReq() {
    return actionReq;
  }
  @Override
  public String toString() {
    return "MarketplaceJob [id=" + id + ", actionReq=" + actionReq + "]";
  }
  
}
