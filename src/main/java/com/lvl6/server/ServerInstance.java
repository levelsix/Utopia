package com.lvl6.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.Message;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.hazelcast.core.Hazelcast;

public class ServerInstance implements InitializingBean{
	
	protected static Logger log = Logger.getLogger(ServerInstance.class);
	
	protected BlockingQueue<Message<?>> serverInstanceEventQueue;
	protected QueueChannel messagesToPlayersOnThisServerQueueChannel;
	protected PollingConsumer messagesToPlayersConsumer;
	protected MessageHandler messageToPlayerHandler;
	protected PeriodicTrigger messagePollingTrigger;
	
	
	/** 
	 * Create a HazelCast queue to listen for messages to players
	 * that are connected to this machine instance
	 * Also create a spring integration channel adapter to 
	 * process these events
	 * 
	 * @throws UnknownHostException
	 */
	public void setup() throws UnknownHostException{
		serverInstanceEventQueue = Hazelcast.getQueue(getServerName());
		messagesToPlayersOnThisServerQueueChannel = new QueueChannel(serverInstanceEventQueue);
		messagePollingTrigger = new PeriodicTrigger(1);
		messagesToPlayersConsumer = new PollingConsumer(messagesToPlayersOnThisServerQueueChannel, messageToPlayerHandler);
		messagesToPlayersConsumer.setTrigger(messagePollingTrigger);
		messagesToPlayersConsumer.start();
	}
	
	
	public String getServerName() throws UnknownHostException{
		return InetAddress.getLocalHost().getHostName();
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Setting up ServerInstance: "+getServerName());
		setup();
	}
		
}
