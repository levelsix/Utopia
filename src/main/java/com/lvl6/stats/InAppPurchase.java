package com.lvl6.stats;

import java.util.Date;

public class InAppPurchase {
	protected Integer userId;
	protected Double cashSpent;
	protected Date purchasedDate;
	protected String userName;

	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Double getCashSpent() {
		return cashSpent;
	}
	public void setCashSpent(Double cashSpent) {
		this.cashSpent = cashSpent;
	}
	public Date getPurchasedDate() {
		return purchasedDate;
	}
	public void setPurchasedDate(Date purchasedDate) {
		this.purchasedDate = purchasedDate;
	}
}
