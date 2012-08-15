package com.lvl6.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

public class ServerInstance implements InitializingBean, MessageListener<Message<?>>, HazelcastInstanceAware  {

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

	@Resource(name="outgoingGameEventsHandlerExecutor")
	protected TaskExecutor executor;
	
	
	public TaskExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(TaskExecutor executor) {
		this.executor = executor;
	}

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
		serverInstanceOutboundEventTopic = hazel.getTopic(getOutboundMessageTopicForServer(serverId()));
		serverInstanceOutboundEventTopic.addMessageListener(this);
	}

	
	public static String getOutboundMessageTopicForServer(String serverId) {
		return serverId+outboundMessagesTopicPostFix;
	}
	
	private String hostName = "";

	public String serverId(){
		if (hostName.equals("")) {
			setServerId();
		}
		return hostName;
	}
	
	protected void setServerId() {
		File hostn = new File("/etc/hostname");
		if(hostn.exists() && hostn.canRead()) {
			try {
				hostName = new Scanner(hostn).useDelimiter(
				"\\Z").next();
			} catch (FileNotFoundException e) {
				log.error(e);
	            log.error("Setting serverId to random UUID");
	            hostName = UUID.randomUUID().toString();
			}
		}else {
			try {
	            Runtime rt = Runtime.getRuntime();
	            Process pr = rt.exec("hostname");
	            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	            String line=null;
	            while((line=input.readLine()) != null) {
	                hostName = line;
	            }
	            int exitVal = pr.waitFor();
	            if(exitVal > 0) {
	            	log.error("Getting hostname failed with error code "+exitVal);
	            	log.error("Setting serverId to random UUID");
	            	hostName = UUID.randomUUID().toString();
	            }
	        } catch(Exception e) {
	            log.error(e);
	            log.error("Setting serverId to random UUID");
	            hostName = UUID.randomUUID().toString();
	        }
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Setting up ServerInstance: " + serverId());
		setup();
	}

	@Override
	public void onMessage(final com.hazelcast.core.Message<Message<?>> message) {
		log.debug("ServerInstance.onMessage");
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					log.info("Sending outbound message to "+message.getMessageObject().getHeaders().get("ip_connection_id")+" from server: "+hostName);
					getOutboundMessageChannel().send(message.getMessageObject());
				}catch(Exception e) {
					log.error("Error sending message", e);
				}
			}
		});
	}

	public MessageChannel getOutboundMessageChannel() {
		return outboundMessageChannel;
	}

	public void setOutboundMessageChannel(MessageChannel outboundMessageChannel) {
		this.outboundMessageChannel = outboundMessageChannel;
	}
	
	protected HazelcastInstance hazel;
	@Override
	@Autowired
	public void setHazelcastInstance(HazelcastInstance instance) {
		hazel = instance;
	}


}
