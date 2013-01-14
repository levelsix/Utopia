package com.lvl6.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.server.ApplicationMode;

public class AmqpServerEventHandler implements MessageListener {

	
	private static final Logger log = LoggerFactory.getLogger(AmqpServerEventHandler.class);
	
	@Autowired
	JsonMessageConverter jsonConverter;
	public JsonMessageConverter getJsonConverter() {
		return jsonConverter;
	}
	public void setJsonConverter(JsonMessageConverter jsonConverter) {
		this.jsonConverter = jsonConverter;
	}

	@Autowired
	ApplicationMode appMode;
	public ApplicationMode getAppMode() {
		return appMode;
	}
	public void setAppMode(ApplicationMode appMode) {
		this.appMode = appMode;
	}

	@Override
	public void onMessage(Message message) {
		ApplicationMode mode = (ApplicationMode) jsonConverter.fromMessage(message);
		appMode.setMaintenanceMode(mode.isMaintenanceMode());
		appMode.setMessageForUsers(mode.getMessageForUsers());
		log.warn("Set Application maintainence mode: {} with message: {}", mode.isMaintenanceMode(), mode.getMessageForUsers());
	}

}
