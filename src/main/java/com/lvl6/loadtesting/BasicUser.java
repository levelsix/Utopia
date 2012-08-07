package com.lvl6.loadtesting;

import com.lvl6.proto.InfoProto.UserType;

public class BasicUser {
	protected Integer userId;
	protected UserType userType;
	protected String udid;

	
	public String getUdid() {
		return udid;
	}
	public void setUdid(String udid) {
		this.udid = udid;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public UserType getUserType() {
		return userType;
	}
	public void setUserType(UserType userType) {
		this.userType = userType;
	}
}
