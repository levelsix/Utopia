package com.lvl6.server;

import java.io.Serializable;

public class ApplicationMode implements Serializable {
	/**
	 * 
	 */
	protected boolean isMaintenanceMode = false;
	protected String messageForUsers = "";

	
	public boolean isMaintenanceMode() {
		return isMaintenanceMode;
	}
	public void setMaintenanceMode(boolean isMaintenanceMode) {
		this.isMaintenanceMode = isMaintenanceMode;
	}
	public String getMessageForUsers() {
		return messageForUsers;
	}
	public void setMessageForUsers(String messageForUsers) {
		this.messageForUsers = messageForUsers;
	}
}
