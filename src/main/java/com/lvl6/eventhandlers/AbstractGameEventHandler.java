package com.lvl6.eventhandlers;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

import com.lvl6.events.RequestEvent;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.ApplicationMode;
import com.lvl6.server.GameServer;
import com.lvl6.server.controller.EventController;
import com.lvl6.utils.Attachment;
import com.lvl6.utils.MessagingUtil;

public abstract class AbstractGameEventHandler implements MessageHandler {

	protected static Logger log = LoggerFactory.getLogger(AbstractGameEventHandler.class);

	@Autowired
	ApplicationMode applicationMode;

	public ApplicationMode getApplicationMode() {
		return applicationMode;
	}

	public void setApplicationMode(ApplicationMode applicationMode) {
		this.applicationMode = applicationMode;
	}

	@Autowired
	MessagingUtil messagingUtil;

	public MessagingUtil getMessagingUtil() {
		return messagingUtil;
	}

	public void setMessagingUtil(MessagingUtil messagingUtil) {
		this.messagingUtil = messagingUtil;
	}

	@Autowired
	GameServer server;

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

	@Override
	public void handleMessage(Message<?> msg) throws MessagingException {
		log.debug("Received message: ");
		for (String key : msg.getHeaders().keySet()) {
			log.debug(key + ": " + msg.getHeaders().get(key));
		}
		// log.info("Payload: "+msg.getPayload());
		Attachment attachment = new Attachment();
		byte[] payload = (byte[]) msg.getPayload();
		attachment.readBuff = ByteBuffer.wrap(payload);
		while (attachment.eventReady()) {
			RequestEvent event = getEvent(attachment);
			log.debug("Recieved event from client: " + event.getPlayerId());
			delegateEvent(payload, event, (String) msg.getHeaders().get("ip_connection_id"),
					attachment.eventType);
			attachment.reset();

		}
	}

	/**
	 * read an event from the attachment's payload
	 */
	protected RequestEvent getEvent(Attachment attachment) {
		RequestEvent event = null;
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(attachment.payload, attachment.payloadSize));

		// get the controller and tell it to instantiate an event for us
		EventController ec = server.getEventControllerByEventType(attachment.eventType);

		if (ec == null) {
			return null;
		}
		event = ec.createRequestEvent();
		event.setTag(attachment.tag);

		// read the event from the payload
		event.read(bb);
		return event;
	}

	protected abstract void delegateEvent(byte[] bytes, RequestEvent event, String ip_connection_id,
			EventProtocolRequest eventType);

}