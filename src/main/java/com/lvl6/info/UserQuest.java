package com.lvl6.info;

import java.io.Serializable;

public class UserQuest implements Serializable {

	private static final long serialVersionUID = 6402831762354203777L;
	private int userId;
	private int questId;
	private boolean isRedeemed;
	private boolean isComplete;
	private boolean tasksComplete;
	private boolean defeatTypeJobsComplete;
	private int coinsRetrievedForReq;

	public UserQuest(int userId, int questId, boolean isRedeemed,
			boolean isComplete, boolean tasksComplete,
			boolean defeatTypeJobsComplete, int coinsRetrievedForReq) {
		super();
		this.userId = userId;
		this.questId = questId;
		this.isRedeemed = isRedeemed;
		this.isComplete = isComplete;
		this.tasksComplete = tasksComplete;
		this.defeatTypeJobsComplete = defeatTypeJobsComplete;
		this.coinsRetrievedForReq = coinsRetrievedForReq;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getQuestId() {
		return questId;
	}

	public void setQuestId(int questId) {
		this.questId = questId;
	}

	public boolean isRedeemed() {
		return isRedeemed;
	}

	public void setRedeemed(boolean isRedeemed) {
		this.isRedeemed = isRedeemed;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public boolean isTasksComplete() {
		return tasksComplete;
	}

	public void setTasksComplete(boolean tasksComplete) {
		this.tasksComplete = tasksComplete;
	}

	public boolean isDefeatTypeJobsComplete() {
		return defeatTypeJobsComplete;
	}

	public void setDefeatTypeJobsComplete(boolean defeatTypeJobsComplete) {
		this.defeatTypeJobsComplete = defeatTypeJobsComplete;
	}

	public int getCoinsRetrievedForReq() {
		return coinsRetrievedForReq;
	}

	public void setCoinsRetrievedForReq(int coinsRetrievedForReq) {
		this.coinsRetrievedForReq = coinsRetrievedForReq;
	}

	@Override
	public String toString() {
		return "UserQuest [userId=" + userId + ", questId=" + questId
				+ ", isRedeemed=" + isRedeemed + ", isComplete=" + isComplete
				+ ", tasksComplete=" + tasksComplete
				+ ", defeatTypeJobsComplete=" + defeatTypeJobsComplete
				+ ", coinsRetrievedForReq=" + coinsRetrievedForReq + "]";
	}
}
