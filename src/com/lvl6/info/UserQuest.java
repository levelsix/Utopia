package com.lvl6.info;

public class UserQuest {
  private int userId;
  private int questId;
  private boolean isRedeemed;
  private boolean tasksComplete;
  private boolean defeatTypeJobsComplete;
  private boolean marketplaceJobsComplete;
  
  public UserQuest(int userId, int questId, boolean isRedeemed,
      boolean tasksComplete, boolean defeatTypeJobsComplete,
      boolean marketplaceTypeJobsComplete) {
    this.userId = userId;
    this.questId = questId;
    this.isRedeemed = isRedeemed;
    this.tasksComplete = tasksComplete;
    this.defeatTypeJobsComplete = defeatTypeJobsComplete;
    this.marketplaceJobsComplete = marketplaceTypeJobsComplete;
  }

  public int getUserId() {
    return userId;
  }

  public int getQuestId() {
    return questId;
  }

  public boolean isRedeemed() {
    return isRedeemed;
  }

  public boolean isTasksComplete() {
    return tasksComplete;
  }

  public boolean isDefeatTypeJobsComplete() {
    return defeatTypeJobsComplete;
  }

  public boolean isMarketplaceTypeJobsComplete() {
    return marketplaceJobsComplete;
  }
  
  public void setTasksComplete(boolean tasksComplete) {
    this.tasksComplete = tasksComplete;
  }

  public void setDefeatTypeJobsComplete(boolean defeatTypeJobsComplete) {
    this.defeatTypeJobsComplete = defeatTypeJobsComplete;
  }

  public void setMarketplaceJobsComplete(boolean marketplaceJobsComplete) {
    this.marketplaceJobsComplete = marketplaceJobsComplete;
  }

  @Override
  public String toString() {
    return "UserQuest [userId=" + userId + ", questId=" + questId
        + ", isRedeemed=" + isRedeemed + ", tasksComplete=" + tasksComplete
        + ", defeatTypeJobsComplete=" + defeatTypeJobsComplete
        + ", marketplaceTypeJobsComplete=" + marketplaceJobsComplete + "]";
  }

}
