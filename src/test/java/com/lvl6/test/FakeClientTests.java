package com.lvl6.test;

import java.nio.ByteBuffer;
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
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupRequestProto.Builder;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.scriptsjava.generatefakeusers.GenerateFakeUsersWithoutInput;
import com.lvl6.utils.Attachment;
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
	public void testFakeClientStartup(){
/*		Builder builder = StartupRequestProto.newBuilder();
		builder.setUdid("A_fake_client");
		byte[] messageBytes = builder.build().toByteArray();
		Message<byte[]> startupMessage = new GenericMessage<byte[]>(messageBytes);
		sendToServer.send(startupMessage);
		Message<?> msg = serverResponses.receive(10000);
		log.info("Received message: ");
		for (String key: msg.getHeaders().keySet()) {
			log.info(key+": "+msg.getHeaders().get(key));
		}
		//log.info("Payload: "+msg.getPayload());
		Attachment attachment = new Attachment();
		attachment.readBuff = ByteBuffer.wrap((byte[]) msg.getPayload());
		while(attachment.eventReady()) {
			ResponseEvent response = new StartupResponseEvent(null);
			response.setTag(attachment.tag);
			//response.re
        }*/
	}
}

