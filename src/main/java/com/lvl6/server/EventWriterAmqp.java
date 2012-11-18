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
		byte[] buff = getByteArray(event);
		MessageProperties msgProps = new MessageProperties();
		String routingKey = "client_udid_" + udid;
		log.info("writing predb event with type=" + event.getEventType() + " to player with routingKey "
				+ routingKey + ", event=" + event);
		clientsTemplate.send(routingKey, new Message(buff, msgProps));
	}

	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	public void processResponseEvent(ResponseEvent event) {
		log.debug("writer received event=" + event);
		byte[] buff = getByteArray(event);
		MessageProperties msgProps = new MessageProperties();
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					String routingKey = "client_userid_" + recipients[i];
					log.info("writing broadcast event with type=" + event.getEventType()
							+ " to players with routingKey " + routingKey);
					sendMessageToPlayer(buff, msgProps, routingKey);
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			String routingKey = "client_userid_" + playerId;
			log.info("writing normal event with type=" + event.getEventType() + " to player with routingKey "
					+ routingKey + ", event=" + event);
			sendMessageToPlayer(buff, msgProps, routingKey);
		}
	}

	protected void sendMessageToPlayer(byte[] buff, MessageProperties msgProps, String routingKey) {
		clientsTemplate.send(routingKey, new Message(buff, msgProps));
	}

	public void processClanResponseEvent(GameEvent event, int clanId) {
		/*
		 * log.debug("writer received clan event=" + event); ResponseEvent e =
		 * (ResponseEvent) event; ByteBuffer buff = getBytes(e); List<UserClan>
		 * playersInClan =
		 * userClanRetrieveUtil.getUserClanMembersInClan(clanId); for (UserClan
		 * uc : playersInClan) {
		 * log.info("Sending response to clan: {}  member: {}"
		 * ,uc.getClanId(),uc.getUserId()); //sendMessageToPlayer(e,
		 * buff.duplicate(), uc.getUserId()); }
		 */
	}

	protected ByteBuffer getBytes(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		return writeBuffer;
	}

	protected byte[] getByteArray(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		byte[] b = new byte[writeBuffer.remaining()];
		writeBuffer.get(b);
		return b;
	}

}// EventWriter