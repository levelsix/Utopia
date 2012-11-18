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
	RabbitTemplate	clientsTemplate;

	public RabbitTemplate getClientsTemplate() {
		return clientsTemplate;
	}

	public void setClientsTemplate(RabbitTemplate clientsTemplate) {
		this.clientsTemplate = clientsTemplate;
	}

	@Autowired
	UserClanRetrieveUtils	userClanRetrieveUtil;

	public UserClanRetrieveUtils getUserClanRetrieveUtil() {
		return userClanRetrieveUtil;
	}

	public void setUserClanRetrieveUtil(UserClanRetrieveUtils userClanRetrieveUtil) {
		this.userClanRetrieveUtil = userClanRetrieveUtil;
	}

	private static org.slf4j.Logger	log	= LoggerFactory.getLogger(EventWriterAmqp.class);

	protected void processEvent(GameEvent event) {
		if (event instanceof ResponseEvent)
			processResponseEvent((ResponseEvent) event);

	}

	public void processPreDBResponseEvent(ResponseEvent event, String udid) {
		ByteBuffer buff = getBytes(event);
		MessageProperties msgProps = new MessageProperties();
		log.info("writing predb event with type=" + event.getEventType() + " to player with udid " + udid
				+ ", event=" + event);
		clientsTemplate.send(udid, new Message(buff.duplicate().array(), msgProps));
	}

	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	public void processResponseEvent(ResponseEvent event) {
		log.debug("writer received event=" + event);
		ByteBuffer buff = getBytes(event);
		MessageProperties msgProps = new MessageProperties();
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					log.info("writing broadcast event with type=" + event.getEventType()
							+ " to players with ids " + recipients[i]);
					sendMessageToPlayer(buff, msgProps, recipients[i]);
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			log.info("writing normal event with type=" + event.getEventType() + " to player with id "
					+ playerId + ", event=" + event);
			sendMessageToPlayer(buff, msgProps, playerId);
		}
	}

	protected void sendMessageToPlayer(ByteBuffer buff, MessageProperties msgProps, int playerId) {
		clientsTemplate.send("" + playerId, new Message(buff.duplicate().array(), msgProps));
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

}// EventWriter