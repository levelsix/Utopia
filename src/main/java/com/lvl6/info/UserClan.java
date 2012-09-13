package com.lvl6.info;

import java.util.Date;

import com.lvl6.proto.InfoProto.UserClanStatus;

public class UserClan {
  private int userId;
  private int clanId;
  private UserClanStatus status;
  private Date requestTime;
  public UserClan(int userId, int clanId, UserClanStatus status,
      Date requestTime) {
    super();
    this.userId = userId;
    this.clanId = clanId;
    this.status = status;
    this.requestTime = requestTime;
  }
  public int getUserId() {
    return userId;
  }
  public int getClanId() {
    return clanId;
  }
  public UserClanStatus getStatus() {
    return status;
  }
  public Date getRequestTime() {
    return requestTime;
  }
  @Override
  public String toString() {
    return "UserClan [userId=" + userId + ", clanId=" + clanId + ", status="
        + status + ", requestTime=" + requestTime + "]";
  }
  
}
