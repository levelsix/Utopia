package com.lvl6.server;

import java.nio.ByteBuffer;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;
import com.lvl6.retrieveutils.UserClanRetrieveUtils;
import com.lvl6.utils.NIOUtils;

public class EventWriterAmqp extends EventWriter {

	@Resource(name = "chatMessagesTemplate")
	RabbitTemplate chatTemplate;

	public RabbitTemplate getChatTemplate() {
		return chatTemplate;
	}

	public void setChatTemplate(RabbitTemplate chatTemplate) {
		this.chatTemplate = chatTemplate;
	}

	@Resource(name = "clientMessagesTemplate")
	RabbitTemplate clientsTemplate;

	public RabbitTemplate getClientsTemplate() {
		return clientsTemplate;
	}

	public void setClientsTemplate(RabbitTemplate clientsTemplate) {
		this.clientsTemplate = clientsTemplate;
	}

	@Autowired
	UserClanRetrieveUtils userClanRetrieveUtil;

	public UserClanRetrieveUtils getUserClanRetrieveUtil() {
		return userClanRetrieveUtil;
	}

	public void setUserClanRetrieveUtil(UserClanRetrieveUtils userClanRetrieveUtil) {
		this.userClanRetrieveUtil = userClanRetrieveUtil;
	}

	private static org.slf4j.Logger log = LoggerFactory.getLogger(EventWriterAmqp.class);

	protected void processEvent(GameEvent event) {
		if (event instanceof ResponseEvent)
			processResponseEvent((ResponseEvent) event);

	}

	public void processPreDBResponseEvent(ResponseEvent event, String udid) {
		//log.info("writer received predb event=\n"+event.toString());
		byte[] buff = getByteArray(event);
		MessageProperties msgProps = new MessageProperties();
		String routingKey = "client_udid_" + udid;
		log.debug("writing predb event with type=" + event.getEventType() + " to player with routingKey "+ routingKey + ", event=" + event);
		sendMessageToPlayer(buff, msgProps, routingKey);
	}

	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	public void processResponseEvent(ResponseEvent event) {
		//log.debug("writer received event=\n" + event.toString());
		byte[] buff = getByteArray(event);
		MessageProperties msgProps = new MessageProperties();
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					String routingKey = "client_userid_" + recipients[i];
					log.debug("writing broadcast event with type=" + event.getEventType()
							+ " to players with routingKey " + routingKey);
					sendMessageToPlayer(buff, msgProps, routingKey);
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			String routingKey = "client_userid_" + playerId;
			log.debug("writing normal event with type=" + event.getEventType() + " to player with routingKey "
					+ routingKey + " event=" + event.getClass().getSimpleName());
			sendMessageToPlayer(buff, msgProps, routingKey);
		}
	}

	protected void sendMessageToPlayer(byte[] buff, MessageProperties msgProps, String routingKey) {
		clientsTemplate.send(routingKey, new Message(buff, msgProps));
	}

	@Override
	public void processClanResponseEvent(ResponseEvent event, int clanId) {
		MessageProperties msgProps = new MessageProperties();
		String clanIdString = "clan_" + clanId;
		log.info("Sending clan response event with routing key:" + clanIdString);
		chatTemplate.send(clanIdString, new Message(getByteArray(event), msgProps));
	}

	public void processGlobalChatResponseEvent(ResponseEvent event) {
		MessageProperties msgProps = new MessageProperties();
		String chatGlobalRoutingKey = "chat_global";
    log.info("Sending global event with type="+event.getEventType()+" with routing key:" + chatGlobalRoutingKey);
		chatTemplate.send(chatGlobalRoutingKey, new Message(getByteArray(event), msgProps));
	}

	protected ByteBuffer getBytes(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		return writeBuffer;
	}

	protected byte[] getByteArray(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		int remaining = writeBuffer.remaining();
		//log.info("Got byte[] of size: {}", remaining);
		byte[] b = new byte[remaining];
		writeBuffer.get(b);
		return b;
	}
	
	protected void traceByteArray(byte[] bytes) {
		StringBuffer buf = new StringBuffer();
		for(int i=0;i < 50; i++) {
			buf.append(i);
			buf.append(": ");
			buf.append(Integer.toHexString(bytes[i]));
			buf.append("\n");
		}
		log.info("First 50 bytes:\n{}", buf.toString());
		buf = new StringBuffer();
		for(int i=bytes.length-51;i < bytes.length; i++) {
			buf.append(i);
			buf.append(": ");
			buf.append(Integer.toHexString(bytes[i]));
			buf.append("\n");
		}
		log.info("Last 50 bytes:\n{}", buf.toString());
	}




}// EventWriter