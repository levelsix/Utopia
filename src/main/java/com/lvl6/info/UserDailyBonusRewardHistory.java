package com.lvl6.info;

import java.util.Date;

public class UserDailyBonusRewardHistory {
  
  private int id;
  private int userId;
  private int currencyRewarded;
  private boolean isCoins;
  private int equipIdRewarded;
  private int nthConsecutiveDay;
  private Date dateAwarded;
  
  public UserDailyBonusRewardHistory(int id, int userId, int currencyRewarded,
      boolean isCoins, int equipIdRewarded, int nthConsecutiveDay,
      Date dateAwarded) {
    super();
    this.id = id;
    this.userId = userId;
    this.currencyRewarded = currencyRewarded;
    this.isCoins = isCoins;
    this.equipIdRewarded = equipIdRewarded;
    this.nthConsecutiveDay = nthConsecutiveDay;
    this.dateAwarded = dateAwarded;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getCurrencyRewarded() {
    return currencyRewarded;
  }

  public void setCurrencyRewarded(int currencyRewarded) {
    this.currencyRewarded = currencyRewarded;
  }

  public boolean isCoins() {
    return isCoins;
  }

  public void setCoins(boolean isCoins) {
    this.isCoins = isCoins;
  }

  public int getEquipIdRewarded() {
    return equipIdRewarded;
  }

  public void setEquipIdRewarded(int equipIdRewarded) {
    this.equipIdRewarded = equipIdRewarded;
  }

  public int getNthConsecutiveDay() {
    return nthConsecutiveDay;
  }

  public void setNthConsecutiveDay(int nthConsecutiveDay) {
    this.nthConsecutiveDay = nthConsecutiveDay;
  }

  public Date getDateAwarded() {
    return dateAwarded;
  }

  public void setDateAwarded(Date dateAwarded) {
    this.dateAwarded = dateAwarded;
  }

  @Override
  public String toString() {
    return "UserDailyBonusRewardHistory [id=" + id + ", userId=" + userId
        + ", currencyRewarded=" + currencyRewarded + ", isCoins=" + isCoins
        + ", equipIdRewarded=" + equipIdRewarded + ", nthConsecutiveDay="
        + nthConsecutiveDay + ", dateAwarded=" + dateAwarded + "]";
  }
  
}
