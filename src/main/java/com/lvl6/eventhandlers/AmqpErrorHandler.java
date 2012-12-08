package com.lvl6.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class AmqpErrorHandler implements ErrorHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AmqpErrorHandler.class);
	
	@Override
	public void handleError(Throwable e) {
		log.error("Error processing amqp message", e);
	}

}
