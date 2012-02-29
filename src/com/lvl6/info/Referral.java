package com.lvl6.info;

import java.util.Date;

public class Referral {
  private int referrerId;
  private int newlyReferredId;
  private Date timeOfReferral;
  public Referral(int referrerId, int newlyReferredId, Date timeOfReferral) {
    this.referrerId = referrerId;
    this.newlyReferredId = newlyReferredId;
    this.timeOfReferral = timeOfReferral;
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
  @Override
  public String toString() {
    return "Referral [referrerId=" + referrerId + ", newlyReferredId="
        + newlyReferredId + ", timeOfReferral=" + timeOfReferral + "]";
  }
}
