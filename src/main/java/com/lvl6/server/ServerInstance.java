package com.lvl6.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

public class ServerInstance implements InitializingBean, MessageListener<Message<?>>  {

	protected static Logger log = Logger.getLogger(ServerInstance.class);
	
	protected static String outboundMessagesTopicPostFix = "OutboundMessages";

	
	
	protected ITopic<Message<?>> serverInstanceOutboundEventTopic;

	public ITopic<Message<?>> getServerInstanceOutboundEventTopic() {
		return serverInstanceOutboundEventTopic;
	}

	public void setServerInstanceOutboundEventTopic(
			ITopic<Message<?>> serverInstanceOutboundEventTopic) {
		this.serverInstanceOutboundEventTopic = serverInstanceOutboundEventTopic;
	}

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

	@Autowired
	protected GameServer server;

	
	@Autowired
	protected MessageChannel outboundMessageChannel;
	
	/**
	 * Create a HazelCast queue to listen for messages to players that are
	 * connected to this machine instance Also create a spring integration
	 * channel adapter to process these events
	 * 
	 * @throws FileNotFoundException 
	 */
	public void setup() throws FileNotFoundException {
		serverInstanceOutboundEventTopic = Hazelcast.getTopic(getOutboundMessageTopicForServer(serverId()));
		serverInstanceOutboundEventTopic.addMessageListener(this);
	}

	
	public static String getOutboundMessageTopicForServer(String serverId) {
		return serverId+outboundMessagesTopicPostFix;
	}
	
	protected String hostName = "";

	public String serverId() throws FileNotFoundException {
		if (hostName.equals("")) {
			hostName = new Scanner(new File("/etc/hostname")).useDelimiter(
					"\\Z").next();
		}
		return hostName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Setting up ServerInstance: " + serverId());
		setup();
	}

	@Override
	public void onMessage(com.hazelcast.core.Message<Message<?>> message) {
		getOutboundMessageChannel().send(message.getMessageObject());
	}

	public MessageChannel getOutboundMessageChannel() {
		return outboundMessageChannel;
	}

	public void setOutboundMessageChannel(MessageChannel outboundMessageChannel) {
		this.outboundMessageChannel = outboundMessageChannel;
	}

}
