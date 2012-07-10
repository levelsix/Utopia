package com.lvl6.info;

import java.io.Serializable;

public class UserEquip implements Serializable {

	private static final long serialVersionUID = 7780574642033448043L;
	private int id;
	private int userId;
	private int equipId;

	public UserEquip(int id, int userId, int equipId) {
		this.id = id;
		this.userId = userId;
		this.equipId = equipId;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getEquipId() {
		return equipId;
	}

	@Override
	public String toString() {
		return "UserEquip [id=" + id + ", userId=" + userId + ", equipId="
				+ equipId + "]";
	}

}
