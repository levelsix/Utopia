package com.lvl6.eventhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

import com.lvl6.events.NormalResponseEvent;

public class OutboundErrorMessageEventHandler implements MessageHandler {

	Logger log = Logger.getLogger(getClass());
	
	
	@Resource(name="messagesForDisconnectedPlayers")
	protected Map<Integer, List<Message<?>>> messagesForDisconnectedPlayers;

	
	
	public Map<Integer, List<Message<?>>> getMessagesForDisconnectedPlayers() {
		return messagesForDisconnectedPlayers;
	}



	public void setMessagesForDisconnectedPlayers(
			Map<Integer, List<Message<?>>> messagesForDisconnectedPlayers) {
		this.messagesForDisconnectedPlayers = messagesForDisconnectedPlayers;
	}



	@Override
	public void handleMessage(Message<?> failedMessage) throws MessagingException {
		if(failedMessage.getHeaders().containsKey("playerId")) {
			NormalResponseEvent ev = (NormalResponseEvent) failedMessage.getPayload();
			Integer user = ev.getPlayerId();
			log.info("Failed to send message to user "+user+"... saving in case they reconnect ");
			List<Message<?>> playerPendingMessages;
			if(messagesForDisconnectedPlayers.containsKey(user)) {
				playerPendingMessages = messagesForDisconnectedPlayers.get(user);
			}else {
				playerPendingMessages = new ArrayList<Message<?>>();
				messagesForDisconnectedPlayers.put(user, playerPendingMessages);
			}
			playerPendingMessages.add(failedMessage);
		}
	}

}