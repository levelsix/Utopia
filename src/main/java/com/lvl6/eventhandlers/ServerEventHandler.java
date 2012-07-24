package com.lvl6.eventhandlers;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.lvl6.server.ServerMessage;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ServerEventHandler implements MessageListener<ServerMessage>, InitializingBean {
	
	Logger log = Logger.getLogger(getClass());

	@Resource(name="serverEvents")
	protected ITopic<ServerMessage> topic;
	
	
	public ITopic<ServerMessage> getTopic() {
		return topic;
	}


	public void setTopic(ITopic<ServerMessage> topic) {
		this.topic = topic;
	}


	@Override
	public void onMessage(Message<ServerMessage> msg) {
		log.info("Handling serverEvent of type: "+msg.getMessageObject());
		if(msg.getMessageObject().equals(ServerMessage.RELOAD_STATIC_DATA)) {
			log.info("Reloading all static data");
			MiscMethods.reloadAllRareChangeStaticData();
		}
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Adding serverEvent listener");
		topic.addMessageListener(this);
	}

}
