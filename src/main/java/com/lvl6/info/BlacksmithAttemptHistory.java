package com.lvl6.info;

public class BlacksmithAttemptHistory {

  private BlacksmithAttempt blacksmithAttempt;
  private boolean success;
  
  public BlacksmithAttemptHistory(BlacksmithAttempt blacksmithAttempt,
      boolean success) {
    this.blacksmithAttempt = blacksmithAttempt;
    this.success = success;
  }

  public BlacksmithAttempt getBlacksmithAttempt() {
    return blacksmithAttempt;
  }

  public boolean isSuccess() {
    return success;
  }

  @Override
  public String toString() {
    return "BlacksmithAttemptHistory [blacksmithAttempt=" + blacksmithAttempt
        + ", success=" + success + "]";
  }
  
}
