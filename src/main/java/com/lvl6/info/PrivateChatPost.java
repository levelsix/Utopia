package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class PrivateChatPost implements Serializable {
	protected static final long serialVersionUID = 8450554970377509383L;
  	protected int id;
	protected int posterId;
	protected int recipientId;
	protected Date timeOfPost;
	protected String content;

	public PrivateChatPost(int id, int posterId, int recipientId,
			Date timeOfPost, String content) {
		super();
		this.id = id;
		this.posterId = posterId;
		this.recipientId = recipientId;
		this.timeOfPost = timeOfPost;
		this.content = content;
	}

	public int getId() {
		return id;
	}

	public int getPosterId() {
		return posterId;
	}

	public int getRecipientId() {
		return recipientId;
	}

	public Date getTimeOfPost() {
		return timeOfPost;
	}

	public String getContent() {
		return content;
	}

	public void setTimeOfPost(Date timeOfPost) {
		this.timeOfPost = timeOfPost;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
	@Override
	public String toString() {
		return "PlayerWallPost [id=" + id + ", posterId=" + posterId
				+ ", recipientId=" + recipientId + ", timeOfPost=" + timeOfPost
				+ ", content=" + content + "]";
	}
}
