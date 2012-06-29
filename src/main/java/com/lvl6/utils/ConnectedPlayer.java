package com.lvl6.utils;

import java.io.Serializable;
import java.util.Date;

/**
 * Player.java
 * 
 * Basic Player information
 */
public class ConnectedPlayer implements Serializable {

	private static final long serialVersionUID = -4695628631220580445L;
	
	protected int playerId;
	protected String ip_connection_id = "";
	protected String serverHostName = "";
	protected String udid = "";
	protected Date lastMessageSentToServer = new Date();

	
	public Date getLastMessageSentToServer() {
		return lastMessageSentToServer;
	}

	public void setLastMessageSentToServer(Date lastMessageSentToServer) {
		this.lastMessageSentToServer = lastMessageSentToServer;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getIp_connection_id() {
		return ip_connection_id;
	}

	public void setIp_connection_id(String ip_connection_id) {
		this.ip_connection_id = ip_connection_id;
	}

	public String getServerHostName() {
		return serverHostName;
	}

	public void setServerHostName(String serverHostName) {
		this.serverHostName = serverHostName;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int id) {
		playerId = id;
	}

}
