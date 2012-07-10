package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

import com.lvl6.proto.InfoProto.ExpansionDirection;

public class UserCityExpansionData implements Serializable {
	private static final long serialVersionUID = -3018246069873803048L;
	private int userId;
	private int farLeftExpansions;
	private int farRightExpansions;
	private boolean isExpanding;
	private Date lastExpandTime; // refers to last time the user clicks the
									// upgrade button, not when the last upgrade
									// was complete
	private ExpansionDirection lastExpandDirection;

	public UserCityExpansionData(int userId, int farLeftExpansions,
			int farRightExpansions, boolean isExpanding, Date lastExpandTime,
			ExpansionDirection lastExpandDirection) {
		this.userId = userId;
		this.farLeftExpansions = farLeftExpansions;
		this.farRightExpansions = farRightExpansions;
		this.isExpanding = isExpanding;
		this.lastExpandTime = lastExpandTime;
		this.lastExpandDirection = lastExpandDirection;
	}

	public int getUserId() {
		return userId;
	}

	public int getFarLeftExpansions() {
		return farLeftExpansions;
	}

	public int getFarRightExpansions() {
		return farRightExpansions;
	}

	public boolean isExpanding() {
		return isExpanding;
	}

	public Date getLastExpandTime() {
		return lastExpandTime;
	}

	public ExpansionDirection getLastExpandDirection() {
		return lastExpandDirection;
	}

	public int getTotalNumCompletedExpansions() {
		return farLeftExpansions + farRightExpansions;
	}

	@Override
	public String toString() {
		return "UserCityExpansionData [userId=" + userId
				+ ", farLeftExpansions=" + farLeftExpansions
				+ ", farRightExpansions=" + farRightExpansions
				+ ", isExpanding=" + isExpanding + ", lastExpandTime="
				+ lastExpandTime + ", lastExpandDirection="
				+ lastExpandDirection + "]";
	}

}
