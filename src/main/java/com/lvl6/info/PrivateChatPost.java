package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class PrivateChatPost implements Serializable {
  private static final long serialVersionUID = 8450554970377509383L;
  private int id;
	private int posterId;
	private int recipientId;
	private Date timeOfPost;
	private String content;

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

	@Override
	public String toString() {
		return "PlayerWallPost [id=" + id + ", posterId=" + posterId
				+ ", recipientId=" + recipientId + ", timeOfPost=" + timeOfPost
				+ ", content=" + content + "]";
	}
}
