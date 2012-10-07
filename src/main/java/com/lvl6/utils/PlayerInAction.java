package com.lvl6.utils;

import java.io.Serializable;
import java.util.Date;

public class PlayerInAction implements Serializable {
	private static final long serialVersionUID = 5759697882546340795L;
	
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
	@Override
	public boolean equals(Object obj) {
		PlayerInAction play = ((PlayerInAction) obj);
		return getPlayerId() == play.getPlayerId();
	}
	
	
}
