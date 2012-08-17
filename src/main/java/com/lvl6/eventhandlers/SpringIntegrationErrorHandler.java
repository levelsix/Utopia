package com.lvl6.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;

public class SpringIntegrationErrorHandler {

	Logger log = LoggerFactory.getLogger(SpringIntegrationErrorHandler.class);
	
	public void handleError(Message<?> errorMessage) {
		MessagingException error = ((MessagingException) errorMessage.getPayload());
		log.error("Error processing message", error);
	}
}
