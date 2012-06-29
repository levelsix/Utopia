package com.lvl6.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.Location;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.scriptsjava.generatefakeusers.GenerateFakeUsersWithoutInput;
import com.lvl6.utils.Attachment;
import com.lvl6.utils.ClientAttachment;
import com.lvl6.utils.utilmethods.MiscMethods;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")

public class FakeClientTests {
	private static Logger log = Logger.getLogger(FakeClientTests.class);
	
	
	@Resource(name="outboundFakeClientMessageChannel")
	protected DirectChannel sendToServer;

	@Resource(name="inboundFakeClientChannel")
	protected QueueChannel serverResponses;
	
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

	
	@Test
	public void testFakeClientStartup() throws InterruptedException{
		Builder builder = StartupRequestProto.newBuilder();
		builder.setUdid("A_fake_client");
		builder.setVersionNum(1.0f);
		byte[]  bytes = builder.build().toByteArray();
		ByteBuffer bb = ByteBuffer.allocate(bytes.length+12);
		bb.putInt(EventProtocolRequest.C_STARTUP_EVENT_VALUE);
		bb.putInt(99);
		bb.putInt(bytes.length);
		bb.put(bytes);
		Message<byte[]> startupMessage = new GenericMessage<byte[]>(bb.array());
		sendToServer.send(startupMessage);
		
		//wait(5000);
		waitForMessage();
	}
	
	
	protected void waitForMessage() {
		Message<?> msg = serverResponses.receive(1500);
		if(msg != null && msg.getHeaders() != null) {
			log.info("Received response message...size: "+ ((byte[]) msg.getPayload()).length);
			for (String key: msg.getHeaders().keySet()) {
				log.info(key+": "+msg.getHeaders().get(key));
			}
			//log.info("Payload: "+msg.getPayload());
			ClientAttachment attachment = new ClientAttachment();
			attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload()).order(ByteOrder.LITTLE_ENDIAN);
			while(attachment.eventReady()) {
				ResponseEvent response = new StartupResponseEvent(null);
				response.setTag(attachment.tag);
				log.info("Received startupResponseEvent");
			}
		}
	}
	
}

