package com.lvl6.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;

import com.lvl6.loadtesting.LoadTestEventGenerator;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.utils.ClientAttachment;

public class HealthCheckImpl implements HealthCheck {
	
	
	private static Logger log = LoggerFactory.getLogger(HealthCheckImpl.class);

	public DirectChannel getSendToServer() {
		return sendToServer;
	}

	public void setSendToServer(DirectChannel sendToServer) {
		this.sendToServer = sendToServer;
	}

	public QueueChannel getServerResponses() {
		return serverResponses;
	}

	public void setServerResponses(QueueChannel serverResponses) {
		this.serverResponses = serverResponses;
	}

	@Resource(name="outboundFakeClientMessageChannel")
	protected DirectChannel sendToServer;

	@Resource(name="inboundFakeClientChannel")
	protected QueueChannel serverResponses;
	
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
		MinimumUserProto.Builder user = gen.minimumUserProto(ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL, UserType.BAD_ARCHER);
		sendToServer.send(gen.userQuestDetails(user));
		return waitForMessage();
	}
	
	protected boolean waitForMessage() {
		Message<?> msg = serverResponses.receive(2000);
		if(msg != null && msg.getHeaders() != null) {
			log.info("Received response message...size: "+ ((byte[]) msg.getPayload()).length);
			for (String key: msg.getHeaders().keySet()) {
				log.info(key+": "+msg.getHeaders().get(key));
			}
			//log.info("Payload: "+msg.getPayload());
			ClientAttachment attachment = new ClientAttachment();
			attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload()).order(ByteOrder.LITTLE_ENDIAN);
			while(attachment.eventReady()) {
				log.info("Received health check response on server: {}", server.serverId());
				return true;
			}
		}
		return false;
		
	}
}
