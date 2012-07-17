package com.lvl6.eventhandlers;

import javax.annotation.Resource;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.lvl6.server.ServerMessage;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ServerEventHandler implements MessageListener<ServerMessage> {

	@Resource(name="serverEvents")
	protected ITopic topic;
	
	
	public ITopic getTopic() {
		return topic;
	}


	public void setTopic(ITopic topic) {
		this.topic = topic;
		topic.addMessageListener(this);
	}


	@Override
	public void onMessage(Message<ServerMessage> msg) {
		if(msg.getMessageObject().equals(ServerMessage.RELOAD_STATIC_DATA)) {
			MiscMethods.reloadAllRareChangeStaticData();
		}
	}

}
