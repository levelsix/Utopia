package com.lvl6.info;

import com.lvl6.proto.InfoProto.UserClanStatus;

public class UserClan {
  private int userId;
  private int clanId;
  private UserClanStatus status;
  public UserClan(int userId, int clanId, UserClanStatus status) {
    this.userId = userId;
    this.clanId = clanId;
    this.status = status;
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
  @Override
  public String toString() {
    return "UserClan [userId=" + userId + ", clanId=" + clanId + ", status="
        + status + "]";
  }
}
