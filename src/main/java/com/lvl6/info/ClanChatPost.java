package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class ClanChatPost implements Serializable {
  private static final long serialVersionUID = -7441177529235760896L;
  private int id;
  private int posterId;
  private int clanId;
  private Date timeOfPost;
  private String content;

  public ClanChatPost(int id, int posterId, int clanId,
      Date timeOfPost, String content) {
    super();
    this.id = id;
    this.posterId = posterId;
    this.clanId = clanId;
    this.timeOfPost = timeOfPost;
    this.content = content;
  }

  public int getId() {
    return id;
  }

  public int getPosterId() {
    return posterId;
  }

  public int getClanId() {
    return clanId;
  }

  public Date getTimeOfPost() {
    return timeOfPost;
  }

  public String getContent() {
    return content;
  }

  @Override
  public String toString() {
    return "ClanChatPost [id=" + id + ", posterId=" + posterId
        + ", clanID=" + clanId + ", timeOfPost=" + timeOfPost
        + ", content=" + content + "]";
  }
}
