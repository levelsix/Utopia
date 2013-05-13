package com.lvl6.info;

import java.util.Date;

public class AdminChatPost extends PrivateChatPost {
	public AdminChatPost(int id, int posterId, int recipientId, Date timeOfPost, String content) {
		super(id, posterId, recipientId, timeOfPost, content);
		//setUsername(username);
	}
	
	protected String username = "";
	

	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}

	private static final long serialVersionUID = -4608572851669225658L;
	
}
