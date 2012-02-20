package com.lvl6.info;

import java.util.Date;

public class MarketplaceTransaction {
  private MarketplacePost post;
  private int buyerId;
  private Date timeOfPurchase;
  
  public MarketplaceTransaction(MarketplacePost post, int buyerId,
      Date timeOfPurchase) {
    this.post = post;
    this.buyerId = buyerId;
    this.timeOfPurchase = timeOfPurchase;
  }

  public MarketplacePost getPost() {
    return post;
  }

  public int getBuyerId() {
    return buyerId;
  }

  public Date getTimeOfPurchase() {
    return timeOfPurchase;
  }

  @Override
  public String toString() {
    return "MarketplaceCompletedTransaction [post=" + post + ", buyerId="
        + buyerId + ", timeOfPurchase=" + timeOfPurchase + "]";
  }
}
