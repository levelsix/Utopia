package com.lvl6.eventhandlers;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

public class OutboundErrorMessageEventHandler implements MessageHandler {

	@Override
	public void handleMessage(Message<?> failedMessage) throws MessagingException {

	}

}
