package com.lvl6.eventhandlers;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.lvl6.server.ServerMessage;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ServerEventHandler implements MessageListener<ServerMessage> {
	
	Logger log = Logger.getLogger(getClass());

	@Resource(name="serverEvents")
	protected ITopic topic;
	
	
	public ITopic getTopic() {
		return topic;
	}


	public void setTopic(ITopic topic) {
		this.topic = topic;
		log.info("Adding serverEvent listener");
		topic.addMessageListener(this);
	}


	@Override
	public void onMessage(Message<ServerMessage> msg) {
		log.info("Handling serverEvent of type: "+msg.getMessageObject());
		if(msg.getMessageObject().equals(ServerMessage.RELOAD_STATIC_DATA)) {
			log.info("Reloading all static data");
			MiscMethods.reloadAllRareChangeStaticData();
		}
	}

}
