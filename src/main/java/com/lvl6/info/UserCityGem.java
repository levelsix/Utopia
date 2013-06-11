package com.lvl6.info;

import java.io.Serializable;

public class UserCityGem implements Serializable {

  private static final long serialVersionUID = -1732793561237109713L;
  private int userId;
	private int cityId;
	private int gemId;
  private int quantity;
  
  
  public UserCityGem(int userId, int cityId, int gemId, int quantity) {
    super();
    this.userId = userId;
    this.cityId = cityId;
    this.gemId = gemId;
    this.quantity = quantity;
  }
  
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public int getCityId() {
    return cityId;
  }
  public void setCityId(int cityId) {
    this.cityId = cityId;
  }
  public int getGemId() {
    return gemId;
  }
  public void setGemId(int gemId) {
    this.gemId = gemId;
  }
  public int getQuantity() {
    return quantity;
  }
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
  
  @Override
  public String toString() {
    return "UserCityGem [userId=" + userId + ", cityId=" + cityId + ", gemId="
        + gemId + ", quantity=" + quantity + "]";
  }
}
