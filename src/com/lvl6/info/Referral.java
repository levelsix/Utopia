package com.lvl6.info;

import java.util.Date;

public class Referral {
  private int referrerId;
  private int newlyReferredId;
  private Date timeOfReferral;
  private int coinsGivenToReferrer;
  public Referral(int referrerId, int newlyReferredId, Date timeOfReferral, int coinsGivenToReferrer) {
    this.referrerId = referrerId;
    this.newlyReferredId = newlyReferredId;
    this.timeOfReferral = timeOfReferral;
    this.coinsGivenToReferrer = coinsGivenToReferrer;
  }
  public int getReferrerId() {
    return referrerId;
  }
  public int getNewlyReferredId() {
    return newlyReferredId;
  }
  public Date getTimeOfReferral() {
    return timeOfReferral;
  }
  public int getCoinsGivenToReferrer() {
    return coinsGivenToReferrer;
  }
  @Override
  public String toString() {
    return "Referral [referrerId=" + referrerId + ", newlyReferredId="
        + newlyReferredId + ", timeOfReferral=" + timeOfReferral
        + ", coinsGivenToReferrer=" + coinsGivenToReferrer + "]";
  }
}
