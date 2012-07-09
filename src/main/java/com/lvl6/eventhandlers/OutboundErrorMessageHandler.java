package com.lvl6.eventhandlers;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

public class OutboundErrorMessageHandler implements MessageHandler {

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		
	}

}
