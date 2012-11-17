package com.lvl6.eventhandlers;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.Attachment;

public class AmqpGameEventHandler extends AbstractGameEventHandler implements MessageListener {

	static Logger log = LoggerFactory.getLogger(GameEventHandler.class);

	
	@Override
	public void onMessage(Message msg) {
		log.info("Received message");
		if(msg != null) {
			Attachment attachment = new Attachment();
			byte[] payload = (byte[]) msg.getBody();
			attachment.readBuff = ByteBuffer.wrap(payload);
			while (attachment.eventReady()) {
				processAttachment(attachment);
				attachment.reset();
			}
		}else {
			throw new RuntimeException("Message was null or missing headers");
		}
	}

	
	protected void processAttachment(Attachment attachment) {
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(attachment.payload, attachment.payloadSize));
		EventController ec = getServer().getEventControllerByEventType(attachment.eventType);
		if (ec == null) {
			log.error("No event controller found in controllerMap for {}", attachment.eventType);
			return;
		}
		RequestEvent  event = ec.createRequestEvent();
		event.setTag(attachment.tag);
		event.read(bb);
		log.debug("Received event from client: " + event.getPlayerId());
		ec.handleEvent(event);
	}

	@Override
	protected void delegateEvent(byte[] bytes, RequestEvent event,
			String ip_connection_id, EventProtocolRequest eventType) {
		if (event != null && eventType.getNumber() < 0) {
			log.error("the event type is < 0");
			return;
		}
		EventController ec = server.getEventControllerByEventType(eventType);
		if (ec == null) {
			log.error("No EventController for eventType: " + eventType);
			return;
		}
		ec.handleEvent(event);
	}

	

}
