package com.lvl6.info;

public class UserQuest {
  private int userId;
  private int questId;
  private boolean isRedeemed;
  private boolean tasksComplete;
  private boolean defeatTypeJobsComplete;
  
  public UserQuest(int userId, int questId, boolean isRedeemed,
      boolean tasksComplete, boolean defeatTypeJobsComplete) {
    this.userId = userId;
    this.questId = questId;
    this.isRedeemed = isRedeemed;
    this.tasksComplete = tasksComplete;
    this.defeatTypeJobsComplete = defeatTypeJobsComplete;
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

  public void setTasksComplete(boolean tasksComplete) {
    this.tasksComplete = tasksComplete;
  }

  public void setDefeatTypeJobsComplete(boolean defeatTypeJobsComplete) {
    this.defeatTypeJobsComplete = defeatTypeJobsComplete;
  }

  @Override
  public String toString() {
    return "UserQuest [userId=" + userId + ", questId=" + questId
        + ", isRedeemed=" + isRedeemed + ", tasksComplete=" + tasksComplete
        + ", defeatTypeJobsComplete=" + defeatTypeJobsComplete + "]";
  }

}
