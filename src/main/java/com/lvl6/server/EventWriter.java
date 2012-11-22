package com.lvl6.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.events.ResponseEvent;
import com.lvl6.utils.Wrap;

public abstract class EventWriter extends Wrap {

	
	private static Logger log = LoggerFactory.getLogger(EventWriter.class);
	public EventWriter() {
		super();
	}

	public abstract void processGlobalChatResponseEvent(ResponseEvent event);

	public abstract void processPreDBResponseEvent(ResponseEvent event, String udid);
	
	public void handleClanEvent(ResponseEvent event, int clanId) {
		try {
			processClanResponseEvent(event, clanId);
		} catch (Exception e) {
			log.error("Error handling clan event: " + event, e);
		}
	}

	public abstract void processClanResponseEvent(ResponseEvent event, int clanId);
	
	
}