package com.lvl6.info;

import java.util.Date;

import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.MarketplacePostType;

public class MarketplacePost {
  
  private int id;
  private int posterId;
  private MarketplacePostType postType;
  private Date timeOfPost;
  private int postedEquipId = ControllerConstants.NOT_SET;
  private int diamondCost = ControllerConstants.NOT_SET;
  private int coinCost = ControllerConstants.NOT_SET;
  
  public MarketplacePost(int id, int posterId, MarketplacePostType postType,
      Date timeOfPost, int postedEquipId, int diamondCost, int coinCost) {
    this.id = id;
    this.posterId = posterId;
    this.postType = postType;
    this.timeOfPost = timeOfPost;
    this.postedEquipId = postedEquipId;
    this.diamondCost = diamondCost;
    this.coinCost = coinCost;
  }
  
  public int getId() {
    return id;
  }
  public int getPosterId() {
    return posterId;
  }
  public MarketplacePostType getPostType() {
    return postType;
  }
  public Date getTimeOfPost() {
    return timeOfPost;
  }
  public int getPostedEquipId() {
    return postedEquipId;
  }
  public int getDiamondCost() {
    return diamondCost;
  }
  public int getCoinCost() {
    return coinCost;
  }

  @Override
  public String toString() {
    return "MarketplacePost [id=" + id + ", posterId=" + posterId
        + ", postType=" + postType + ", timeOfPost="
        + timeOfPost + ", postedEquipId=" + postedEquipId
        + ", diamondCost=" + diamondCost
        + ", coinCost=" + coinCost + "]";
  }  
}