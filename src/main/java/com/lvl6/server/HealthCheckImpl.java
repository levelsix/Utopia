package com.lvl6.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;

import com.lvl6.loadtesting.LoadTestEventGenerator;
import com.lvl6.properties.Globals;
import com.lvl6.utils.ClientAttachment;

public class HealthCheckImpl implements HealthCheck {
	
	
	private static final int ALERT_INTERVAL = 1000*60*5;

	private static Logger log = LoggerFactory.getLogger(HealthCheckImpl.class);

	public MessageChannel getSendToServer() {
		return sendToServer;
	}

	public void setSendToServer(MessageChannel sendToServer) {
		this.sendToServer = sendToServer;
	}

	public QueueChannel getServerResponses() {
		return serverResponses;
	}

	public void setServerResponses(QueueChannel serverResponses) {
		this.serverResponses = serverResponses;
	}
	
	
	@Resource
	protected DevOps devops;
	

	public DevOps getDevops() {
		return devops;
	}

	public void setDevops(DevOps devops) {
		this.devops = devops;
	}


	@Resource(name="outboundFakeClientMessageChannel")
	protected MessageChannel sendToServer;

	@Resource(name="inboundFakeClientChannel")
	protected QueueChannel serverResponses;
	
	
	@Resource
	protected TcpNioServerConnectionFactory serverConnectionFactory;
	
	
	public TcpNioServerConnectionFactory getServerConnectionFactory() {
		return serverConnectionFactory;
	}

	public void setServerConnectionFactory(
			TcpNioServerConnectionFactory serverConnectionFactory) {
		this.serverConnectionFactory = serverConnectionFactory;
	}

	@Resource
	protected ServerInstance server;
	
	public ServerInstance getServer() {
		return server;
	}

	public void setServer(ServerInstance server) {
		this.server = server;
	}

	public LoadTestEventGenerator getGen() {
		return gen;
	}

	public void setGen(LoadTestEventGenerator gen) {
		this.gen = gen;
	}

	@Resource
	protected LoadTestEventGenerator gen;
	
	
	
	/* (non-Javadoc)
	 * @see com.lvl6.server.HealthCheck#check()
	 */
	@Override
	public boolean check() {
		log.info("Running health check");
		sendToServer.send(gen.startup("Cluster Server Instance: "+server.serverId()));
		//sendToServer.send(gen.userQuestDetails(user));
		return waitForMessage();
	}
	
	
	protected int failsSinceLastSuccess = 0;
	protected boolean waitForMessage() {
		Message<?> msg = serverResponses.receive(Globals.HEALTH_CHECK_TIMEOUT()*1000);
		if(msg != null && msg.getHeaders() != null) {
			log.info("Received response message...size: "+ ((byte[]) msg.getPayload()).length);
			ClientAttachment attachment = new ClientAttachment();
			attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload()).order(ByteOrder.LITTLE_ENDIAN);
			while(attachment.eventReady()) {
				log.info("Received health check response on server: {}", server.serverId());
				failsSinceLastSuccess = 0;
				return true;
			}
		}
		if(!serverConnectionFactory.isListening() || !serverConnectionFactory.isRunning()) {
			log.warn("ServerConnectionFactory stopped running or listening... restarting");
			serverConnectionFactory.run();
		}
		failsSinceLastSuccess++;
		if(failsSinceLastSuccess > 2) {
			sendAlertToAdmins();
		}
		return false;
		
	}
	
	protected void sendAlertToAdmins() {
		if(devops.lastAlertSentToAdmins == null || new Date().getTime() > devops.getLastAlertSentToAdmins().getTime()+ALERT_INTERVAL) {
			log.warn("Contacting admins to notify of failed healthcheck");
			devops.sendAlertToAdmins("Health check failed on one or more server instances");
		}else {
			log.warn("Not contacting admins because contact interval threshold has not been exceeded");
		}
	}

	@Override
	public void logCurrentSystemInfo() {
	    Runtime runtime = Runtime.getRuntime();
	    NumberFormat format = NumberFormat.getInstance();
	    StringBuilder sb = new StringBuilder();
	    long maxMemory = runtime.maxMemory();
	    long allocatedMemory = runtime.totalMemory();
	    long freeMemory = runtime.freeMemory();
	    long cpus = runtime.availableProcessors();
	    sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
	    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
	    sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
	    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
	    sb.append("total cpus: " + cpus + "\n");
	    log.info("System info: {}", sb.toString());
	}
}
