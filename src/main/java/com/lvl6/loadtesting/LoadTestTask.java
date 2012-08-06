package com.lvl6.loadtesting;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.server.controller.EventController;

public class LoadTestTask implements Runnable {
	
	protected String udid;
	protected Integer userId;
	protected byte[] event;
	protected Integer eventType;
	protected GameServer server;
	
	protected RequestEvent originalEvent;
	protected RequestEvent newEvent;
	
	public LoadTestTask(String udid, Integer userId, byte[] event,
			Integer eventType, GameServer server) {
		super();
		this.udid = udid;
		this.userId = userId;
		this.event = event;
		this.eventType = eventType;
		this.server = server;
	}


	public Integer getEventType() {
		return eventType;
	}
	
	
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}
	
	
	public GameServer getServer() {
		return server;
	}
	
	
	public void setServer(GameServer server) {
		this.server = server;
	}

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
	public byte[] getEvent() {
		return event;
	}
	public void setEvent(byte[] event) {
		this.event = event;
	}


	@Override
	public void run() {
		convertBytesToEvent();
		updatePropertiesOfEvent();
		sendEventToServer();
	}
	
	public void convertBytesToEvent() {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(event, event.length));
		EventProtocolRequest evType = EventProtocolRequest.valueOf(eventType);
		EventController ec = server.getEventControllerByEventType(evType);
		originalEvent = ec.createRequestEvent();
		originalEvent.setTag(event.length);
		originalEvent.read(bb);
	}
	
	public void updatePropertiesOfEvent() {
		originalEvent.
	}
	
	public void sendEventToServer() {
		
	}
	
}
