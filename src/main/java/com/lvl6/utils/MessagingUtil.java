package com.lvl6.utils;

import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.server.EventWriter;

public class MessagingUtil {
	
	@Autowired
	EventWriter eventWriter;

	public EventWriter getEventWriter() {
		return eventWriter;
	}

	public void setEventWriter(EventWriter eventWriter) {
		this.eventWriter = eventWriter;
	}
	
	
	
	public void sendAdminMessage(String message) {
		//send admin message
		
		//send regular global chat
		
	}
	
	
	
}
