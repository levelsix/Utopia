package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.Message;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.ReconnectRequestEvent;
import com.lvl6.events.response.ReconnectResponseEvent;
import com.lvl6.proto.EventProto.ReconnectRequestProto;
import com.lvl6.proto.EventProto.ReconnectResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.EventWriter;

@Component
@DependsOn("gameServer")
public class ReconnectController extends EventController {

	private Logger log = LoggerFactory.getLogger(getClass());

	public ReconnectController() {
		numAllocatedThreads = 4;
	}

	@Resource(name = "messagesForDisconnectedPlayers")
	protected Map<Integer, List<Message<?>>> messagesForDisconnectedPlayers;

	public Map<Integer, List<Message<?>>> getMessagesForDisconnectedPlayers() {
		return messagesForDisconnectedPlayers;
	}

	public void setMessagesForDisconnectedPlayers(
			Map<Integer, List<Message<?>>> messagesForDisconnectedPlayers) {
		this.messagesForDisconnectedPlayers = messagesForDisconnectedPlayers;
	}
	
	
	@Autowired
	protected EventWriter eventWriter;
	
	
	
	public EventWriter getEventWriter() {
		return eventWriter;
	}

	public void setEventWriter(EventWriter eventWriter) {
		this.eventWriter = eventWriter;
	}

	@Override
	public RequestEvent createRequestEvent() {
		return new ReconnectRequestEvent();
	}

	@Override
	public EventProtocolRequest getEventType() {
		return EventProtocolRequest.C_RECONNECT_EVENT;
	}

	@Override
	protected void processRequestEvent(RequestEvent event) throws Exception {
		ReconnectRequestProto reqProto = ((ReconnectRequestEvent) event)
				.getReconnectRequestProto();

		MinimumUserProto senderProto = reqProto.getSender();

		ReconnectResponseProto.Builder resBuilder = ReconnectResponseProto
				.newBuilder();
		resBuilder.setSender(senderProto);
		resBuilder.setIncomingResponseMessages(false);
		Integer userId = event.getPlayerId();
		
		ReconnectResponseProto resProto = resBuilder.build();
		ReconnectResponseEvent resEvent = new ReconnectResponseEvent(
				senderProto.getUserId());
		resEvent.setTag(event.getTag());
		resEvent.setReconnectResponseProto(resProto);
		server.writeEvent(resEvent);
	}

}
