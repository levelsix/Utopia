package com.lvl6.utils;

import java.util.Date;

public class PlayerInAction {
	
	protected int playerId;
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public Date getLockTime() {
		return lockTime;
	}
	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}
	protected Date lockTime = new Date();
	
	public PlayerInAction(int playerId){
		this.playerId = playerId;
	}
}
