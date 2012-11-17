package com.lvl6.server;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;

import com.lvl6.events.BroadcastResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.info.UserClan;
import com.lvl6.properties.Globals;
import com.lvl6.retrieveutils.UserClanRetrieveUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.Wrap;

public class EventWriterAmqp extends Wrap {

	@Autowired
	UserClanRetrieveUtils userClanRetrieveUtil;

	public UserClanRetrieveUtils getUserClanRetrieveUtil() {
		return userClanRetrieveUtil;
	}

	public void setUserClanRetrieveUtil(UserClanRetrieveUtils userClanRetrieveUtil) {
		this.userClanRetrieveUtil = userClanRetrieveUtil;
	}

	private static org.slf4j.Logger log = LoggerFactory.getLogger(EventWriter.class);

	protected void processEvent(GameEvent event) {
		if (event instanceof ResponseEvent)
			processResponseEvent((ResponseEvent) event);

	}

	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	public void processResponseEvent(ResponseEvent event) {
		log.debug("writer received event=" + event);
		ByteBuffer buff = getBytes(event);
		if (BroadcastResponseEvent.class.isInstance(event)) {
			int[] recipients = ((BroadcastResponseEvent) event).getRecipients();
			for (int i = 0; i < recipients.length; i++) {
				if (recipients[i] > 0) {
					log.info("writing broadcast event with type="
							+ event.getEventType() 
							+ " to players with ids "
							+ recipients[i]);
					log.info("writing normal event with type="
							+ event.getEventType() 
							+ " to player with id "
							+ recipients[i] 
							+ ", event=" 
							+ event);
					//write(buff.duplicate(), player);
				}
			}
		}
		// Otherwise this is just a normal message, send response to sender.
		else {
			int playerId = ((NormalResponseEvent) event).getPlayerId();
			sendMessageToPlayer(event, buff, playerId);
		}

	}

	protected void sendMessageToPlayer(ResponseEvent event, ByteBuffer buff,
			int playerId) {
		log.info("writing event with type=" 
			+ event.getEventType()
			+ " to player with id " 
			+ playerId 
			+ ", event=" 
			+ event);
		//write(buff, player);
	}

	public void processClanResponseEvent(GameEvent event, int clanId) {
		log.debug("writer received clan event=" + event);
		ResponseEvent e = (ResponseEvent) event;
		ByteBuffer buff = getBytes(e);
		List<UserClan> playersInClan = userClanRetrieveUtil.getUserClanMembersInClan(clanId);
		for (UserClan uc : playersInClan) {
			log.info("Sending response to clan: {}  member: {}",uc.getClanId(),uc.getUserId());
			sendMessageToPlayer(e, buff.duplicate(), uc.getUserId());
		}
	}

	protected ByteBuffer getBytes(ResponseEvent event) {
		ByteBuffer writeBuffer = ByteBuffer	.allocateDirect(Globals.MAX_EVENT_SIZE);
		NIOUtils.prepBuffer(event, writeBuffer);
		return writeBuffer;
	}


	public void sendMessageToPlayer(Message<?> message, Integer playerId) {

	}



}// EventWriter