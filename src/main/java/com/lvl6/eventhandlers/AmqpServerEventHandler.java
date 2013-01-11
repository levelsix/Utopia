package com.lvl6.eventhandlers;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class AmqpServerEventHandler implements MessageListener {

	@Override
	public void onMessage(Message message) {
		// TODO add message handling logic for Maintanence messages

	}

}
