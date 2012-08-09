package com.lvl6.loadtesting;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.eventhandlers.AbstractGameEventHandler;
import com.lvl6.eventhandlers.GameEventHandler;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

public class GameEventSaveHandler extends AbstractGameEventHandler  {
	
	protected static Logger log = LoggerFactory.getLogger(GameEventSaveHandler.class);
	@Resource
	protected GameEventRecorder recorder;
	
	public GameEventRecorder getRecorder() {
		return recorder;
	}

	public void setRecorder(GameEventRecorder recorder) {
		this.recorder = recorder;
	}

	
	@Override
	protected void delegateEvent(byte[] bytes, RequestEvent event, String ip_connection_id, EventProtocolRequest eventType) {
		log.info("Persisting event for load testing");
		recorder.persistEvent(event.getPlayerId(), eventType.getNumber(), bytes);
	}
	
}
