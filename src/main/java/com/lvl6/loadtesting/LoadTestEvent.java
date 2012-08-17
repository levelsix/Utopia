package com.lvl6.loadtesting;

import java.sql.Timestamp;

public class LoadTestEvent {
	
	Integer userId;
	Integer eventType;
	Timestamp eventTime;
	byte[] event;

	
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getEventType() {
		return eventType;
	}
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}
	public Timestamp getEventTime() {
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
	public byte[] getEvent() {
		return event;
	}
	public void setEvent(byte[] event) {
		this.event = event;
	}
}
