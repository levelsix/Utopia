package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class ClanBulletinPost implements Serializable {
  private static final long serialVersionUID = 5288364470565668989L;
  private int id;
  private int posterId;
  private int clanId;
  private Date timeOfPost;
  private String content;

  public ClanBulletinPost(int id, int posterId, int clanId,
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
    return "ClanBulletinPost [id=" + id + ", posterId=" + posterId
        + ", clanID=" + clanId + ", timeOfPost=" + timeOfPost
        + ", content=" + content + "]";
  }
}
