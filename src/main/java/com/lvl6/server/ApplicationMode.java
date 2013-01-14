package com.lvl6.server;

public class ApplicationMode {
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
