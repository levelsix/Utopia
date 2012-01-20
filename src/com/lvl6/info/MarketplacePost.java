package com.lvl6.info;

import java.util.Date;

import com.lvl6.proto.InfoProto.MarketplacePostType;

public class MarketplacePost {
  
  public static final int NOT_SET = -1;
  
  private int id;
  private int posterId;
  private MarketplacePostType postType;
  private Date timeOfPost;
  private int postedEquipId = NOT_SET;
  private int postedWood = NOT_SET;
  private int postedDiamonds = NOT_SET;
  private int postedCoins = NOT_SET;
  private int diamondCost = NOT_SET;
  private int coinCost = NOT_SET;
  private int woodCost = NOT_SET;
  
  public MarketplacePost(int id, int posterId, MarketplacePostType postType,
      Date timeOfPost, int postedEquipId, int postedWood, int postedDiamonds,
      int postedCoins, int diamondCost, int coinCost, int woodCost) {
    this.id = id;
    this.posterId = posterId;
    this.postType = postType;
    this.timeOfPost = timeOfPost;
    this.postedEquipId = postedEquipId;
    this.postedWood = postedWood;
    this.postedDiamonds = postedDiamonds;
    this.postedCoins = postedCoins;
    this.diamondCost = diamondCost;
    this.coinCost = coinCost;
    this.woodCost = woodCost;
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
  public int getPostedWood() {
    return postedWood;
  }
  public int getPostedDiamonds() {
    return postedDiamonds;
  }
  public int getPostedCoins() {
    return postedCoins;
  }
  public int getDiamondCost() {
    return diamondCost;
  }
  public int getCoinCost() {
    return coinCost;
  }
  public int getWoodCost() {
    return woodCost;
  }

  @Override
  public String toString() {
    return "MarketplacePost [id=" + id + ", posterId=" + posterId
        + ", postType=" + postType + ", timeOfPost="
        + timeOfPost + ", postedEquipId=" + postedEquipId
        + ", postedWood=" + postedWood + ", postedDiamonds=" + postedDiamonds
        + ", postedCoins=" + postedCoins + ", diamondCost=" + diamondCost
        + ", coinCost=" + coinCost + ", woodCost=" + woodCost + "]";
  }  
}
