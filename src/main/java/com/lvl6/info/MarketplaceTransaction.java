package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class MarketplaceTransaction implements Serializable {
	private static final long serialVersionUID = 7343585725112273270L;
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
