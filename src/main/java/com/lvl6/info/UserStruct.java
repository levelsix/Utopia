package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

import com.lvl6.proto.InfoProto.StructOrientation;

public class UserStruct implements Serializable {
  //any change in this class should also change the UpdateUtils.java method updateUserStructsLastretrieved()
	private static final long serialVersionUID = 117707363672187583L;
	private int id;
	private int userId;
	private int structId;
	private Date lastRetrieved;
	private CoordinatePair coordinates;
	private int level;
	private Date purchaseTime;
	private Date lastUpgradeTime;
	private boolean isComplete;
	private StructOrientation orientation;

	public UserStruct(int id, int userId, int structId, Date lastRetrieved,
			CoordinatePair coordinates, int level, Date purchaseTime,
			Date lastUpgradeTime, boolean isComplete,
			StructOrientation orientation) {
		this.id = id;
		this.userId = userId;
		this.structId = structId;
		this.lastRetrieved = lastRetrieved;
		this.coordinates = coordinates;
		this.level = level;
		this.purchaseTime = purchaseTime;
		this.lastUpgradeTime = lastUpgradeTime;
		this.isComplete = isComplete;
		this.orientation = orientation;
	}

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getStructId() {
		return structId;
	}

	public Date getLastRetrieved() {
		return lastRetrieved;
	}

	public CoordinatePair getCoordinates() {
		return coordinates;
	}

	public int getLevel() {
		return level;
	}

	public Date getPurchaseTime() {
		return purchaseTime;
	}

	public Date getLastUpgradeTime() {
		return lastUpgradeTime;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public StructOrientation getOrientation() {
		return orientation;
	}

	@Override
	public String toString() {
		return "UserStruct [id=" + id + ", userId=" + userId + ", structId="
				+ structId + ", lastRetrieved=" + lastRetrieved
				+ ", coordinates=" + coordinates + ", level=" + level
				+ ", purchaseTime=" + purchaseTime + ", lastUpgradeTime="
				+ lastUpgradeTime + ", isComplete=" + isComplete
				+ ", orientation=" + orientation + "]";
	}

}
