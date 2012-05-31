package com.lvl6.info;

import java.util.Date;

public class PlayerWallPost {
  private int id;
  private int posterId;
  private int wallOwnerId;
  private Date timeOfPost;
  private String content;
  public PlayerWallPost(int id, int posterId, int wallOwnerId, Date timeOfPost,
      String content) {
    super();
    this.id = id;
    this.posterId = posterId;
    this.wallOwnerId = wallOwnerId;
    this.timeOfPost = timeOfPost;
    this.content = content;
  }
  public int getId() {
    return id;
  }
  public int getPosterId() {
    return posterId;
  }
  public int getWallOwnerId() {
    return wallOwnerId;
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
        + ", wallOwnerId=" + wallOwnerId + ", timeOfPost=" + timeOfPost
        + ", content=" + content + "]";
  }
}
