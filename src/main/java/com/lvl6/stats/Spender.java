package com.lvl6.stats;

public class Spender {
	protected Integer userId;
  protected Double amountSpent;
  protected String name;
	
	public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Double getAmountSpent() {
		return amountSpent;
	}
	public void setAmountSpent(Double amountSpent) {
		this.amountSpent = amountSpent;
	}
	
}
